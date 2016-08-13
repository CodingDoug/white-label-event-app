/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hyperaware.conference.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.data.FirebaseDatabaseHelpers;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.android.util.Strings;
import com.hyperaware.conference.android.view.FeedbackCardView;
import com.hyperaware.conference.android.view.MutexViewGroup;
import com.hyperaware.conference.android.view.RatingFeedbackCardView;
import com.hyperaware.conference.android.view.TextFeedbackCardView;
import com.hyperaware.conference.android.view.YesNoFeedbackCardView;
import com.hyperaware.conference.model.AgendaItem;
import com.hyperaware.conference.model.AgendaSection;
import com.hyperaware.conference.model.FeedbackQuestion;
import com.hyperaware.conference.model.SpeakerItem;
import com.hyperaware.conference.model.SpeakersSection;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Logger;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class SessionFeedbackFragment extends Fragment implements Titled {

    private static final Logger LOGGER = Logging.getLogger(SessionFeedbackFragment.class);

    private static final String ARG_SESSION_ID = "session_id";
    private static final String ARG_TITLE = "title";

    private final FirebaseDatabase fdb = FirebaseDatabase.getInstance();
    private final DatabaseReference questionsRef = fdb.getReference("/feedback/session_questions");
    private DatabaseReference answersRef;
    private final QuestionsListener questionsListener = new QuestionsListener();
    private final AnswersListener answersListener = new AnswersListener();

    private String sessionId;
    private String title;

    private Bus bus;

    private MutexViewGroup vgMutex;
    private TextView tvTopic;
    private ViewGroup vgSpeakers;
    private ViewGroup vgQuestions;
    private ArrayList<FeedbackCardView> allCards = new ArrayList<>();

    private AgendaItem agendaItem;
    private SpeakersSection speakers;
    private List<FeedbackQuestion> questions;
    private List<Object> priorAnswers;

    @NonNull
    public static SessionFeedbackFragment instantiate(@NonNull final String sessionId, @NonNull final String title) {
        if (Strings.isNullOrEmpty(sessionId)) {
            throw new IllegalArgumentException(ARG_SESSION_ID + " can't be null or empty");
        }

        final Bundle args = new Bundle();
        args.putString(ARG_SESSION_ID, sessionId);
        args.putString(ARG_TITLE, title);

        final SessionFeedbackFragment fragment = new SessionFeedbackFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        sessionId = args.getString(ARG_SESSION_ID);
        if (Strings.isNullOrEmpty(sessionId)) {
            throw new IllegalArgumentException(ARG_SESSION_ID + " can't be null or empty");
        }
        title = args.getString(ARG_TITLE);

        bus = Singletons.deps.getBus();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            answersRef = fdb.getReference("/feedback/sessions/" + sessionId + "/" + user.getUid());
        }
        else {
            // WTF, we should never have gotten here
            getFragmentManager().popBackStack();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session_feedback, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        if (activity instanceof ContentHost) {
            ((ContentHost) activity).setTitle(title);
        }

        final View root = getView();
        if (root == null) {
            throw new IllegalStateException();
        }

        vgMutex = (MutexViewGroup) root.findViewById(R.id.vg_mutex);
        tvTopic = (TextView) vgMutex.findViewById(R.id.tv_topic);
        vgSpeakers = (ViewGroup) vgMutex.findViewById(R.id.vg_speakers);
        vgQuestions = (ViewGroup) vgMutex.findViewById(R.id.vg_questions);
        final Button buttonRemoveFeedback = (Button) vgMutex.findViewById(R.id.button_remove_feedback);
        buttonRemoveFeedback.setOnClickListener(new RemoveFeedbackOnClickListener());
        final Button buttonSendFeedback = (Button) root.findViewById(R.id.button_send_feedback);
        buttonSendFeedback.setOnClickListener(new SendFeedbackOnClickListener());

        updateUi();
    }

    @Override
    public void onStart() {
        super.onStart();
        bus.register(this);

        questionsRef.addListenerForSingleValueEvent(questionsListener);
        answersRef.addListenerForSingleValueEvent(answersListener);
    }

    @Override
    public void onStop() {
        questionsRef.removeEventListener(questionsListener);
        answersRef.removeEventListener(answersListener);

        bus.unregister(this);
        super.onStop();
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    private class RemoveFeedbackOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            answersRef.removeValue();
            getFragmentManager().popBackStack();
        }
    }

    private class SendFeedbackOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final ArrayList<Object> answers = new ArrayList<>();
            for (FeedbackCardView card : allCards) {
                if (card instanceof RatingFeedbackCardView) {
                    final float rating = ((RatingFeedbackCardView) card).getRating();
                    if (rating > 0) {
                        answers.add(rating);
                    }
                    else {
                        answers.add("");
                    }
                }
                else if (card instanceof YesNoFeedbackCardView) {
                    final Boolean choice = ((YesNoFeedbackCardView) card).getChoice();
                    if (choice != null) {
                        answers.add(choice);
                    }
                    else {
                        answers.add("");
                    }
                }
                else if (card instanceof TextFeedbackCardView) {
                    final CharSequence text = ((TextFeedbackCardView) card).getText();
                    if (text.length() > 0) {
                        answers.add(text.toString());
                    }
                    else {
                        answers.add("");
                    }
                }
            }
            LOGGER.fine(answers.toString());
            answersRef.setValue(answers);

            Toast.makeText(getActivity(), R.string.msg_feedback_thank_you, Toast.LENGTH_SHORT).show();
            getFragmentManager().popBackStack();
        }
    }

    @Subscribe
    public void onAgenda(final AgendaSection agenda) {
        if (agenda != null) {
            // TODO rarely ever a session not found
            final AgendaItem item = agenda.getItems().get(sessionId);
            if (agendaItem == null || !agendaItem.equals(item)) {
                agendaItem = item;
                updateUi();
            }
        }
    }

    @Subscribe
    public void onSpeakers(final SpeakersSection speakers) {
        if (speakers != null) {
            this.speakers = speakers;
            updateUi();
        }
    }

    private class QuestionsListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot data) {
            LOGGER.fine("Feedback questions");
            LOGGER.fine(data.toString());
            questions = FirebaseDatabaseHelpers.toFeedbackQuestions(data);
            updateUi();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    }

    private class AnswersListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            LOGGER.fine("Answers");
            LOGGER.fine(dataSnapshot.toString());
            priorAnswers = dataSnapshot.getValue(new GenericTypeIndicator<List<Object>>() {});
            if (priorAnswers == null) {
                priorAnswers = new ArrayList<>();
            }
            updateUi();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    }

    private void updateUi() {
        if (agendaItem != null && speakers != null && questions != null && priorAnswers != null) {
            vgMutex.showViewId(R.id.content);
            updateSessionFeedback();
        }
        else {
            vgMutex.showViewId(R.id.pb);
        }
    }

    private void updateSessionFeedback() {
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        tvTopic.setText(agendaItem.getTopic());

        final Map<String, SpeakerItem> speakersItems = speakers.getItems();
        vgSpeakers.removeAllViews();
        for (final String id : agendaItem.getSpeakerIds()) {
            final SpeakerItem item = speakersItems.get(id);
            if (item != null) {
                TextView tv_speaker = (TextView) inflater.inflate(R.layout.inc_feedback_speaker, vgSpeakers, false);
                tv_speaker.setText(item.getName());
                vgSpeakers.addView(tv_speaker);
            }
        }

        vgQuestions.removeAllViews();
        final ListIterator<FeedbackQuestion> it = questions.listIterator();
        while (it.hasNext()) {
            int i = it.nextIndex();
            final FeedbackQuestion question = it.next();

            Object prior = null;
            if (priorAnswers.size() > i) {
                prior = priorAnswers.get(i);
            }

            final FeedbackCardView card;
            switch (question.getType()) {
            case Rate5:
                card = (FeedbackCardView) inflater.inflate(R.layout.card_feedback_question_rate, vgQuestions, false);
                if (prior instanceof Number) {
                    final RatingFeedbackCardView ratingcv = (RatingFeedbackCardView) card;
                    ratingcv.setRating(((Number) prior).floatValue());
                }
                break;
            case YN:
                card = (FeedbackCardView) inflater.inflate(R.layout.card_feedback_question_yn, vgQuestions, false);
                if (prior instanceof Boolean) {
                    final YesNoFeedbackCardView yncv = (YesNoFeedbackCardView) card;
                    yncv.setChoice((Boolean) prior);
                }
                break;
            case Text:
            default:
                card = (FeedbackCardView) inflater.inflate(R.layout.card_feedback_question_text, vgQuestions, false);
                if (prior instanceof String) {
                    final TextFeedbackCardView tcv = (TextFeedbackCardView) card;
                    tcv.setText((String) prior);
                }
                break;
            }
            card.setQuestionText(question.getText());
            allCards.add(card);
            vgQuestions.addView(card);
        }
    }

}

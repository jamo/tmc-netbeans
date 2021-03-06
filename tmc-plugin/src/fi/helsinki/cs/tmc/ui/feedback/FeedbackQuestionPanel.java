package fi.helsinki.cs.tmc.ui.feedback;

import fi.helsinki.cs.tmc.data.FeedbackAnswer;
import javax.swing.JPanel;

public abstract class FeedbackQuestionPanel extends JPanel {
    /**
     * Constructs an answer from the UI state, or null if no answer provided.
     */
    public abstract FeedbackAnswer getAnswer();
}

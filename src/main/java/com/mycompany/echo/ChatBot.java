// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.mycompany.echo;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.*;
import com.microsoft.bot.schema.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatBot extends ActivityHandler {

    protected final BotState conversationState;
    public static Writer FILEWRITER = null;
    private static final String LOGIN = "Login problems";
    private static final String SERVER = "Server-specific problems";
    private static final String PROGRAM = "Problems with one of our programs";
    private static final String CONTRACT = "Contract issues";
    private static final String PROJECT = "Project-/employee-issues";
    private static final String PASSWORD = "I forgot my password.";
    private static final String USERNAME = "I forgot my username.";
    private static final String DISMISSAL = "Dismissal";
    public static final String ADJUST = "Adjust contract";
    private static final String OTHER = "My problem doesn't match any of the suggested options.";
    private static final String REPLY_OTHER =
            "Please reach out to our support team if you have a complex issue.\n" +
            "Mo - Sa from 8am - 8pm : +49 221 - 6392678\n" +
            "or via e-mail: support@-solutionsit.de";
    public static int TICKET;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final String EMPLOYEE = "talk to an employee.";
    private static final String EMPLOYEE_FEEDBACK = "commend/make a complaint.";
    private static int counter = 0;

    Problems problems = new Problems();

    @Autowired
    public ChatBot(ConversationState withConversationState) {
        conversationState = withConversationState;
    }

    @Override
    protected CompletableFuture<Void> onMembersAdded(List<ChannelAccount> membersAdded, TurnContext turnContext
    ) {
        return membersAdded.stream()
                .filter(
                        member -> !StringUtils
                                .equals(member.getId(), turnContext.getActivity().getRecipient().getId())
                ).map(channel -> sendIntroCard(turnContext))
                .collect(CompletableFutures.toFutureList()).thenApply(resourceResponses -> null);
    }

    CompletableFuture<ResourceResponse> sendIntroCard(TurnContext turnContext) {
        HeroCard card = new HeroCard();
        card.setTitle("Welcome to our customer support via Chatbot!");
        card.setText("Hello! I assist with support-specific questions. How may I help you?");

        List<String> actions = Arrays.asList(LOGIN, SERVER, PROGRAM, CONTRACT, PROJECT, OTHER);

        List<CardAction> cardActions = actions.stream().map(this::returnNewCardAction).collect(Collectors.toList());

        card.setButtons(cardActions);

        Activity response = MessageFactory.attachment(card.toAttachment());
        return turnContext.sendActivity(response);
    }

    private CardAction returnNewCardAction(String type) {
        CardAction action = new CardAction();
        action.setType(ActionTypes.MESSAGE_BACK);
        action.setTitle(type);
        action.setText(type);
        action.setDisplayText(type);
        action.setValue(type);
        return action;
    }

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {

        String userInput = turnContext.getActivity().getText();

        switch (userInput) {
            case LOGIN: {
                return onLoginProblems(turnContext);
            }
            case PASSWORD:
            case USERNAME:
                return onLoginProblems(turnContext, userInput);
            case SERVER:
            case PROGRAM:
                return onServerProblems(turnContext);
            case CONTRACT:
                return onContractIssues(turnContext);
            case DISMISSAL:
            case ADJUST:
                return onContractIssues(turnContext, userInput);
            case PROJECT:
                return problems.sendWorkerCard(turnContext).thenApply(result -> null);
            case OTHER:
                return turnContext.sendActivity(REPLY_OTHER).thenApply(result -> null);
            case EMPLOYEE:
                return problems.sendOtherCard(turnContext).thenApply(result -> null);
            case EMPLOYEE_FEEDBACK:
                return problems.sendAnswerCard(turnContext).thenApply(result -> null);
            default:
                if (isValidMailAddress(userInput)) {
                    turnContext.sendActivity("Thank you! Please check your mail inbox.");
                } else if (userInput.contains("1234")) { //FIXME: regex für String bestehend aus 6 Ziffern
                    onServerProblems(turnContext, userInput);
                } else if (problems.getControl() == 100) {
                    return problems.sendResponseAnswerCard(turnContext).thenApply(result -> null);
                }else if(counter == 1) {
                    try {
                        FILEWRITER.write(turnContext.getActivity().getText());
                        FILEWRITER.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    HeroCard card = new HeroCard();
                    card.setTitle("Thank you for reaching out!");
                    card.setText("We created an intern ticket. You can check the status via ticket number " + TICKET + ".");
                    Activity response = MessageFactory.attachment((card.toAttachment()));
                    return turnContext.sendActivity(response).thenApply(r->null);
                } else {
                    turnContext.sendActivity("Sorry, I didn't catch that.");
                }
        }
        return null;
    }

    private CompletableFuture<Void> onLoginProblems(TurnContext turnContext) {

        Activity reply = MessageFactory.text("Please choose your issue:");

        CardAction passwordAction = new CardAction();
        passwordAction.setTitle(PASSWORD);
        passwordAction.setType(ActionTypes.IM_BACK);
        passwordAction.setValue(PASSWORD);

        CardAction usernameAction = new CardAction();
        usernameAction.setTitle(USERNAME);
        usernameAction.setType(ActionTypes.IM_BACK);
        usernameAction.setValue(USERNAME);

        SuggestedActions actions = new SuggestedActions();
        actions.setActions(Arrays.asList(passwordAction, usernameAction));
        reply.setSuggestedActions(actions);
        return turnContext.sendActivity(reply).thenApply(sendResult -> null);
    }

    private CompletableFuture<Void> onLoginProblems(TurnContext turnContext, String text) {

        switch (text) {
            case PASSWORD:
                return turnContext.sendActivity("Please enter your mail address that you used to login. " +
                        "You will receive a mail to reset your password.").thenApply(result -> null);
            case USERNAME:
                return turnContext.sendActivity("Please enter your mail address that you used to login. " +
                        "You will receive a mail with instructions to reset your login data.").thenApply(result -> null);
            case OTHER:
                return turnContext.sendActivity(REPLY_OTHER).thenApply(r -> null);
            default:
                return turnContext.sendActivity("Sorry, I didn't catch that.").thenApply(r -> null);
        }
    }

    public CompletableFuture<Void> onServerProblems(TurnContext turnContext) {
        return turnContext.sendActivity("Please enter your customer number.").thenApply(r -> null);
    }

    private void onServerProblems(TurnContext turnContext, String userInput) {
        int max = 100;
        TICKET = (int) (Math.random()*max)+1;
        try {
            new File("problemdescription.txt");
            FILEWRITER = new FileWriter("problemdescription.txt");
            FILEWRITER.write("Customer number: " + userInput + "\n" + "Ticket number: " + TICKET + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        counter = 1;
        turnContext.sendActivity("Thank you! Now please describe your problem as exactly as possible so we can help you in the best way.").thenApply(r -> null);
    }

    public static boolean isValidMailAddress(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    private CompletableFuture<Void> onContractIssues(TurnContext turnContext) {
        String text = turnContext.getActivity().getText();
        if (text.equals(CONTRACT)) {
            HeroCard card = new HeroCard();
            card.setText("Please choose your issue:");
            List<String> actions = Arrays.asList(ADJUST, DISMISSAL, OTHER);
            List<CardAction> cardActions = actions.stream().map(this::returnNewCardAction).collect(Collectors.toList());
            card.setButtons(cardActions);
            Activity response = MessageFactory.attachment(card.toAttachment());
            turnContext.sendActivity(response);
        }
        return null;
    }

    private CompletableFuture<Void> onContractIssues(TurnContext turnContext, String userInput) {

        switch (userInput) {
            case DISMISSAL:
                return turnContext.sendActivity("We're sorry to hear that you want to resign. Please fill in our dismissal form." +
                        "It gets forwarded to our support: \" +\n" +
                        "                \"www.solutionsgmbh.de/kuendigung/sendonsave\"").thenApply(r -> null);
            case ADJUST:
                return turnContext.sendActivity("You can adjust contract details via our contract form. If your desired change isn't listed, please reach out directly to our support team." +
                        " \" www.solutionsgmbh.de/changeContractData").thenApply(r -> null);
            case OTHER:
                return turnContext.sendActivity(REPLY_OTHER).thenApply(r -> null);
            default:
                return turnContext.sendActivity("Sorry, I didn't catch that.").thenApply(r -> null);
        }
    }
}
package com.mycompany.echo;

import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.dialogs.*;
import com.microsoft.bot.dialogs.choices.FoundChoice;
import com.microsoft.bot.dialogs.prompts.ConfirmPrompt;
import com.microsoft.bot.dialogs.prompts.PromptOptions;
import com.microsoft.bot.dialogs.prompts.TextPrompt;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class UserProfileDialog extends ComponentDialog {
    private final StatePropertyAccessor<UserProfile> userProfileAccessor;

    public UserProfileDialog(UserState withUserState) {
        super("UserProfileDialog");

        userProfileAccessor = withUserState.createProperty("UserProfile");

        WaterfallStep[] waterfallSteps = {
                this::customerNo,
                this::project
        };

        addDialog(new WaterfallDialog("WaterfallDialog", Arrays.asList(waterfallSteps)));
        addDialog(new TextPrompt("TextPrompt"));

        setInitialDialogId("WaterfallDialog");
    }

    private CompletableFuture<DialogTurnResult> customerNo(WaterfallStepContext stepContext) {
        PromptOptions promptOptions = new PromptOptions();
        promptOptions.setPrompt(MessageFactory.text("Please enter your Customer No."));
        return stepContext.prompt("TextPrompt", promptOptions);
    }

    private CompletableFuture<DialogTurnResult> project(WaterfallStepContext stepContext) {
        stepContext.getValues().put("customerNo", ((FoundChoice) stepContext.getResult()).getValue());
        PromptOptions promptOptions = new PromptOptions();
        promptOptions.setPrompt(MessageFactory.text("Please enter the name of the project related to your problem."));
        return stepContext.prompt("TextPrompt", promptOptions);
    }
}


// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.mycompany.echo;

import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.dialogs.*;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class MainDialog extends ComponentDialog {
    private final UserState userState;

    public MainDialog(UserState withUserState) {
        super("MainDialog");

        userState = withUserState;

        addDialog(new UserProfileDialog(userState));

        WaterfallStep[] waterfallSteps = {
            this::initialStep,
            this::finalStep
        };

        addDialog(new WaterfallDialog("WaterfallDialog", Arrays.asList(waterfallSteps)));

        setInitialDialogId("WaterfallDialog");
    }

    private CompletableFuture<DialogTurnResult> initialStep(WaterfallStepContext stepContext) {
        return stepContext.beginDialog("UserProfileDialog");
    }

    private CompletableFuture<DialogTurnResult> finalStep(WaterfallStepContext stepContext) {
        UserProfile userInfo = (UserProfile) stepContext.getResult();

        String status = "Thank you for letting us know about your troubles! We will get back to you as soon as possible.";

        return stepContext.getContext().sendActivity(status)
            .thenCompose(resourceResponse -> {
                StatePropertyAccessor<UserProfile> userProfileAccessor = userState.createProperty("UserProfile");
                return userProfileAccessor.set(stepContext.getContext(), userInfo);
            })
            .thenCompose(setResult -> stepContext.endDialog());
    }
}
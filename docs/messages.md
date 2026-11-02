# Messages: a snackbar for a result, a dialog for a cause and a next step

On a phone, a message is one of three things. Pick by what the user has to do with it.

| The message is | Use |
| --- | --- |
| The outcome of a tap, in one short sentence | A snackbar (`showSnackbar`) |
| Why something failed and what the user can do about it | `BingeMessageDialog` |
| A load that failed | An error screen with Retry, in place |

The TV has no snackbars. It shows a result in place, and an acknowledgement that is already a dialog is `TvMessageDialog`.

## Snackbar

- One short sentence that reads in full at 1.3x font, for example "Not changed. Couldn't reach your server."
- It may carry one action (Retry, Undo) and goes away by itself.
- A snackbar is never used for text the user must read to act. A guard test (`BingeSnackbarResultFitTest`) holds the example to two lines at 1.3x with nothing cut.

## Message dialog

- A title says what happened. The body gives the cause and the next step. One text button, "OK" by default, acknowledges it.
- It does not go away by itself. The button, Back and a tap outside dismiss it.
- It is an acknowledgement, not a confirmation, so it has exactly one action. A choice is `BingeConfirmDialog`: at most two actions, on one row.
- A long body scrolls inside the dialog rather than clipping.
- A message the user must not lose to a stray tap, such as a secret shown once (an API key), passes `dismissible = false`: Back and a tap outside do nothing, and only the button closes it. Give that button a label that says what closing means, such as "I have copied it".
- What the message is about and the user acts on, such as that key and its Copy button, goes in `extraContent`, under the message and inside the same scroll. The caller owns its state and what the button does. The TV dialog has the same slot, between the message and the button, and focus still arrives on the button.

## Worked example: a failed backup

A backup that was refused for lack of space was shown in a snackbar, and the sentence was cut off mid-way: "The server may need 5 GiB free where it keeps backups. Free up space before tr…". The user needs the cause and the next step to act, so it is a message dialog:

- Title: "The backup wasn't made".
- Body: "The server may need 5 GiB free where it keeps backups. Free up space there, then try again."
- Button: "OK".

The snackbar keeps the other half of the same screen: "Backup created" is a result.

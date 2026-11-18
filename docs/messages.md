# Messages: a snackbar for a result, a dialog for a cause and a next step

On a phone, a message is one of four things. Pick by what the user has to do with it.

| The message is | Use |
| --- | --- |
| The outcome of a tap, in one short sentence | A snackbar (`showSnackbar`) |
| Why something failed and what the user can do about it | `BingeMessageDialog` |
| A load that failed | An error screen with Retry, in place |
| A send that failed, from a sheet's pinned primary button | The footer's failed state, then a dialog for the cause |

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

## A failed send from a sheet's pinned primary

This is for one case: a sheet whose pinned primary button sends something (connect, send, request, report), and the send did not go through. It is not for a page's footer, and not for a load that failed.

- The button turns to the error colour and its label becomes a short reason of a few words, for example "Quota reached". This is the `failed` state of `BingeActionFooter`. The caller passes the reason as the label and decides how long it shows, a few seconds.
- A tap on it is the caller's. It opens a dialog with the full cause and Retry. A dialog with Retry and a way out is a `BingeConfirmDialog`, which has two actions. A `BingeMessageDialog` has exactly one, so it is not the right one.
- A screen reader hears the reason as an error, because a failed footer sets the `error` semantics with the label.
- The reason is a few words, such as "Quota reached", "No permission" or "Couldn't request". The full cause belongs in the dialog. A longer reason wraps and the button grows rather than clip it, as the Spanish frame at 2x font shows, but a few words is what the button is for.
- `destructive` is not this. It is for a sheet whose one action removes or declines something, and it carries no error semantics. A footer that is both gets the same colours.

## Worked example: a failed backup

A backup that was refused for lack of space was shown in a snackbar, and the sentence was cut off mid-way: "The server may need 5 GiB free where it keeps backups. Free up space before tr…". The user needs the cause and the next step to act, so it is a message dialog:

- Title: "The backup wasn't made".
- Body: "The server may need 5 GiB free where it keeps backups. Free up space there, then try again."
- Button: "OK".

The snackbar keeps the other half of the same screen: "Backup created" is a result.

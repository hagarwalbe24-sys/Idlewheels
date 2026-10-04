# IdleWheels manual test checklist

Run the application with the `dev` profile and use `http://localhost:8081/`.

- [ ] Register one Owner and one Renter; verify duplicate email is rejected.
- [ ] Log in and log out as both roles; verify passwords are not stored as plain text.
- [ ] As Owner, add one Car and one Bike; confirm each appears under My vehicles.
- [ ] As Owner, create a listing for each vehicle, including a local photo; confirm the listing appears in My listings.
- [ ] Edit a listing and replace its photo; deactivate a listing and confirm it no longer appears in public search.
- [ ] As Renter, search active listings by city, vehicle type, price range, and availability dates; combine filters.
- [ ] Open a listing detail page and verify vehicle information, availability, price, and photo.
- [ ] As Renter, message the listing owner; verify the conversation appears in both users' inboxes.
- [ ] Reply as Owner; verify the Renter sees the reply and incoming messages become read when the conversation is opened.
- [ ] Try invalid form values and an unauthorized owner action; verify friendly error handling.

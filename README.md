# Bonuskaart.com
[Albert Heijn](https://en.wikipedia.org/wiki/Albert_Heijn) is the largest supermarket chain in the Netherlands. Their customer loyalty program comes in the form of a card, which they call 'Bonuskaart' (hence [Bonuskaart.com](https://bonuskaart.com)) which gives you discount.

[Bonuskaart.com](https://bonuskaart.com) will make sure you always have a Bonuskaart at hand, while also preserving your privacy!

At the moment this project is very much work in progress:
* The front-end is not responsive. The sizes were just picked until it worked on my phone (Please don't look at the website on a desktop/laptop, it's horrible)
* All the bonuskaart barcodes are stored in a text file which is read on startup
* Uploaded barcodes are currently only stored in a log, and will have to be added manually to said text file

## Why
I absolutely love Albert Heijn, the products are good, prices are reasonable, and the service is great! But next to that, I also love privacy. The Bonuskaart is Albert Heijn's customer loyalty card, which upon showing, gives you a discount on selected products.

While Albert Heijn does offer an anonymous Bonuskaart, it's not really anonymous if you're the only person using it. When you scan your Bonuskaart to get your discount, Albert Heijn can link the purchase to you.

Besides leaking information on when and where you shop for which products, it also leaks a tremendous amount of meta data. This meta data can include indicators on your age, gender, level of education, type of household and more. In a collaboration between Albert Heijn and Micrsoft they claim to know what you will buy before you even in the shop [1]. Don't get me wrong, I trust Albert Heijn not to abuse the information they have. However, I believe that it never hurts to reduce your digital footprint.

Often privacy preserving come with a cost in the form of inconvenience. I could of course not use a Bonuskaart, but then I would end up paying more than necessary. I could also ask one of the friendly cashiers if they can scan their bonuskaart, but that's an inconvenience when using the self-scan registers. So I created [Bonuskaart.com](https://bonuskaart.com), a simple website that serves a fresh bonuskaart everyday.

[1] "Albert Heijn - Predict my List" by 'Microsoft Western Europe' at [youtube.com](https://www.youtube.com/watch?v=0heyIKUqdOM)

## How does it work

The privacy preserving functionality of [Bonuskaart.com](https://bonuskaart.com) comes from a combination of two effects.

Firstly, by using a different bonuskaart for every purchase, personal information will be fragmented. If there are n cards in the database, each bonuskaart will only contain 1/nth of a single person's personal information.

The second part comes from the fact that each bonuskaart can be used by an arbitrary amount of people. When trying to identify a single person, every other person using the same bonuskaart adds noise. The more people using the same card, the noisier the data becomes, the more private it becomes.
How does the website work

When you visit [Bonuskaart.com](https://bonuskaart.com) for the first time, it will send a request for a new bonuskaart. Upon reception, the server will randomly select a barcode and send it to the client. When the client receives this bonuskaart, they will store it in a cookie. This cookie will expire at 23:59 of that day.

When visiting the website again, the website will check for cookies. If it finds one, it will use bonsukaart stored in the cookie instead of requesting a new one. The reuse of the same bonuskaart is required for the handheld scanners. To unlock a scanner one scans a bonuskaart, but to checkout you need to scan the same bonuskaart at a payment terminal.

## FAQ:
### Doesn't the cookie store identifiable information?
Yes, but this cookie never leaves your device. The checking for cookies happens on the client (i.e. your smartphone). Take a look at the code, if you want to see for yourself.

### Wouldn't a single card for all the users add more noise?
Yes. However, if two people with the same barcode in the same store, want to use the handheld scanners it may cause problems.
I don't know what the exact behavior would be, but I suspect it would prevent you taking out two scanners with the same bonuskaart. Another possibility is that, at checkout, the system would confuse you for the other customer, making you pay for someone else's groceries.

### Can I host My own version?
Of course. But the anonymity increase if more people use the same Bonuskaart. Especially if you would be the only one using it. Therefore, I would encourage you to use [Bonuskaart.com](https://bonuskaart.com).

### This project is really awesome/horrible, can I contribute?
Absolutely! Feel free to make any changes and do a pull-request!
Especially contributions to the front-end would be appreciated, such making it responsive for example.
For now I just picked some sizes that made it usable on my screen, but I'm sure it won't be as nice on different resolution screens.

### Can I donate a Bonuskaart?
Yes, please do so! 
You can fill in the numbers below your [barcode here](https://bonuskaart.com/donate_bonuskaart.html).
Java.perform(function () {

    var CryptoKeyActivity = Java.use("com.example.trustmebro.CryptoKeyActivity");

    var masterKey = CryptoKeyActivity.getMasterKey();
    var paymentSecret = CryptoKeyActivity.getPaymentTokenSecret();

    console.log("KEYS EXTRACTED FROM MEMORY");
    console.log("Master Key: " + masterKey);
    console.log("Payment Secret: " + paymentSecret);
});
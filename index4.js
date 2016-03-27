
var firebase = require('firebase');
// Get a database reference to our posts
var ref = new Firebase("https://watchdog-app.firebaseio.com/");
// Attach an asynchronous callback to read the data at our posts reference

//////



// Twilio Credentials
var accountSid = 'AC3b2049b1e5d002e20997e811daab884b';
var authToken = '33ec0f54681c6b9c1f8d9c87f35818de';

//require the Twilio module and create a REST client
var client = require('twilio')(accountSid, authToken);

//--------
var express = require('express');
var app = express();


// app.use(bodyParser.json());
// app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static(__dirname, "public"));

// everytime the button is clicked do the following.
app.post('/LEDon3', function(req, res) {
    console.log('LEDON3');
    ref.on("value", function(snapshot) {
  // console.log(snapshot.val());

  var heartrate = snapshot.val().Bill.People.Andrew.heartRate;
  // console.log(heartrate);
  // console.log(something.heartRate[0])

var what = [];
  for (var key in heartrate) {
// console.log(key);
what.push(key);
}


var fun = [];

  for(var key in heartrate){
    var log = "Key: {0} - Value: {1}";
    log = log.replace("{0}", key); // key
    log = log.replace("{1}", heartrate[key]); // value
    fun.push(heartrate[key]);
    // console.log(log);
  }

console.log(fun);

}, function (errorObject) {
  console.log("The read failed: " + errorObject.code);
});



});




// after the button click this will be available
app.post('/LEDon', function(req, res) {
    console.log('texting');
    client.messages.create({
    to: "+16479287149",
    from: "+16475591341",
    body: "Your health condition is very low, would you like to go to the hospital"
}, function(err, message) {
    console.log(message);
});
});

app.post('/LEDon2', function(req, res) {
    console.log('calling');
client.calls.create({
    url: "http://demo.twilio.com/docs/voice.xml",
    to: "+16479287149",
    from: "+16475591341",
    statusCallback: "https://www.myapp.com/events",
    statusCallbackMethod: "POST",
    statusCallbackEvent: ["initiated", "ringing", "answered", "completed"],
    method: "GET"
}, function(err, call) {
    process.stdout.write(call.sid);
});

});








app.get('/', function(req, res){
  res.sendFile(__dirname + '/index4.html');
});





app.listen(6000, function(){
  console.log('listening on *:6000');
});

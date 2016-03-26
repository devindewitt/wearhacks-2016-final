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
  res.sendFile(__dirname + '/index.html');
});



app.listen(5000, function(){
  console.log('listening on *:5000');
});

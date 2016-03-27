$('#ledon-button3').click(function() {
    $.ajax({
        type: 'POST',
        url: 'http://localhost:5000/LEDon3'
    });
});
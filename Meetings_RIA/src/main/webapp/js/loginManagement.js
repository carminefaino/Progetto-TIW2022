/**
 * Login management
 */
(function() {
	var register_div = document.getElementById("register-div");
	var password_input = document.getElementById("registerbutton").closest("form").querySelector('input[name="password"]');
	var repeat_password_input = document.getElementById("registerbutton").closest("form").querySelector('input[name="password2"]');

	document.getElementById("loginbutton").addEventListener('click', (e) => {
		var form = e.target.closest("form");
		if (form.checkValidity()) {
			makeCall("POST", 'CheckLogin', e.target.closest("form"),
				function(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						var message = x.responseText;
						switch (x.status) {
							case 200:
								sessionStorage.setItem('username', message);
								window.location.href = "Home.html";
								break;
							case 400:  //bad request
								document.getElementById("errormessage").textContent = message;
								break;
							case 401:  //unauthorized
								document.getElementById("errormessage").textContent = message;
								break;
							case 500:  //server error
								document.getElementById("errormessage").textContent = message;
								break;
						}
					}
				}
			);
		} else {
			form.reportValidity();
		}
	});


	document.getElementById("open_register_div").addEventListener('click', function(e) {
		if (e.target.textContent === "Registrati!") {
			e.target.textContent = "Chiudi form di registrazione";
			register_div.style.display = 'block';
		} else {
			e.target.textContent = "Registrati!";
			register_div.style.display = 'none';
		}
	});


	document.getElementById("registerbutton").addEventListener('click', (e) => {
		var form = e.target.closest("form");
		document.getElementById("errormessage2").textContent = "";
		if (form.checkValidity()) {
			if (repeat_password_input.value != password_input.value) {
				document.getElementById("errormessage2").textContent = "Le due password non corrispondono";
				return;
			}
			makeCall("POST", 'Register', e.target.closest("form"),
				function(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						var message = x.responseText;
						switch (x.status) {
							case 200:
								var data = JSON.parse(x.responseText);
								sessionStorage.setItem('username', data.username);
								window.location.href = "Home.html";
								break;
							case 400:  //bad request
								document.getElementById("errormessage2").textContent = message;
								break;
							case 401:  //unauthorized
								document.getElementById("errormessage2").textContent = message;
								break;
							case 500:  //server error
								document.getElementById("errormessage2").textContent = message;
								break;
						}
					}
				}
			);
		} else {
			form.reportValidity();
		}
	});

})();
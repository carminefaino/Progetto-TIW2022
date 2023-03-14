{
	var myMeetingsList, otherMeetingsList, setTodayDate, form;
	var pageOrchestrator = new PageOrchestrator();
	var attempts = 0;

	window.addEventListener("load", () => {
		if (sessionStorage.getItem("username") == null) {
			window.location.href = "index.html";
		} else {
			pageOrchestrator.start();     //initialize the components
			pageOrchestrator.refresh();   //display initial content
		}
	}, false);
	
	function WelcomeMessage(_username, messagecontainer) {
		this.username = _username;
		this.show = function() {
			messagecontainer.textContent = this.username;
		}
	}

	var modal = document.getElementById('modal');
	var btn_open_modal = document.getElementById("btn_open_modal");
	var modal_close = document.getElementsByClassName("modal_close")[0];
	var modal_content = document.getElementById("modal_content");

	modal_close.onclick = function() {
		modal.style.display = "none";
	}

	//Chiude la finestra modale quando l'utente clicca al di fuori di essa
	window.onclick = function(event) {
		if (event.target == modal) { modal.style.display = "none"; }
	}


	document.getElementById("anagrafica_button").addEventListener('click', (e) => {
		form = e.target.closest("form");
		if (form.checkValidity()) {
			modal.style.display = "block";

			var showAllUsers = new ShowAllUsers(
				document.getElementById("div_users"),
				document.getElementById("alertUsers"));


			document.getElementById("alertUsers").textContent = "";
			document.getElementById("div_users").innerHTML = "";
			showAllUsers.reset();
			showAllUsers.show();
		} else {
			form.reportValidity();
		}
	});


	document.getElementById("createMeeting_button").addEventListener('click', () => {
		var selectedCheckBoxValue = [];
		var selectedCheckBoxText = [];

		var selectedCheckBox = $(':checkbox:checked').map(function(i) {    //jquery function
			selectedCheckBoxValue[i] = this.value;
			selectedCheckBoxText[i] = $(this).next('label').text();
			return this;
		}).get();


		var maxNumberOfParticipants = document.getElementById("maxNumberOfParticipants").value;

		if (selectedCheckBoxValue.length == 0) {
			document.getElementById("alertUsers").textContent = "Selezionare almeno un utente";
		} else {
			if (selectedCheckBoxValue.length < maxNumberOfParticipants) {
				//convert usersChecked in a string
				let usersChecked = selectedCheckBoxText + "";

				makeCall2("POST", 'CreateMeeting', form, usersChecked,
					function(req) {
						if (req.readyState == XMLHttpRequest.DONE) {
							var message = req.responseText;
							if (req.status == 200) {
								document.getElementById('modal').style.display = "none";
								pageOrchestrator.start();
								pageOrchestrator.refresh();
							} else {
								document.getElementById('modal').style.display = "none";
								document.getElementById("alertForm").textContent = message;
							}
						}
					}
				);
			} else {
				document.getElementById("alertUsers").textContent = "";
				var string = "Troppi utenti selezionati, eliminarne almeno " + (selectedCheckBoxValue.length - (maxNumberOfParticipants - 1));
				document.getElementById("alertUsers").textContent = string;
				attempts += 1;
				if (attempts === 3) {
					alert("Tre tentativi di definire una riunione con troppi partecipanti, la riunione non sarà creata");
					attempts = 0;
					document.getElementById('modal').style.display = "none";
				}
			}
		}

	});


	document.getElementById("deleteMeeting_button").addEventListener('click', () => {
		document.getElementById('modal').style.display = "none";
		document.forms[0].reset();
	});

	function MyMeetingsList(_alert, _listcontainer, _listcontainerbody) {
		this.alert = _alert;
		this.listcontainer = _listcontainer;
		this.listcontainerbody = _listcontainerbody;

		this.reset = function() {
			this.listcontainer.style.visibility = "hidden";
		}

		this.show = function(next) {
			var self = this;
			makeCall("GET", "GetMyMeetings", null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							var meetingsToShow = JSON.parse(req.responseText);
							if (meetingsToShow.length == 0) {
								self.alert.textContent = "Hai creato zero meetings";
								return;
							}
							self.update(meetingsToShow);
							if (next) next();     //show the default element of the list if present
						}

						else {
							self.alert.textContent = message;
						}
					}
				}
			);
		};

		this.update = function(arrayMeetings) {
			var row, destcell, datecell;
			this.listcontainerbody.innerHTML = "";
			
			var self = this;
			arrayMeetings.forEach(function(meeting) {
				row = document.createElement("tr");
				destcell = document.createElement("td");
				destcell.textContent = meeting.title;
				row.appendChild(destcell);
				datecell = document.createElement("td");
				datecell.textContent = meeting.date;
				row.appendChild(datecell);
				datecell = document.createElement("td");
				datecell.textContent = meeting.time;
				row.appendChild(datecell);
				datecell = document.createElement("td");
				datecell.textContent = meeting.duration;
				row.appendChild(datecell);
				datecell = document.createElement("td");
				datecell.textContent = meeting.maxNumberOfParticipants;
				row.appendChild(datecell);
				datecell = document.createElement("td");
				datecell.textContent = meeting.createdBy;
				row.appendChild(datecell);

				self.listcontainerbody.appendChild(row);
			});

			this.listcontainer.style.visibility = "visible";

		}
	}

	function OtherMeetingsList(_alert, _listcontainer, _listcontainerbody) {
		this.alert = _alert;
		this.listcontainer = _listcontainer;
		this.listcontainerbody = _listcontainerbody;

		this.reset = function() {
			this.listcontainer.style.visibility = "hidden";
		}

		this.show = function(next) {
			var self = this;
			makeCall("GET", "GetOtherMeetings", null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							var meetingsToShow = JSON.parse(req.responseText);
							if (meetingsToShow.length == 0) {
								self.alert.textContent = "Sei stato invitato a zero meetings";
								return;
							}
							self.update(meetingsToShow);
							if (next) next();     //show the default element of the list if present

						}

						else {
							self.alert.textContent = message;
						}
					}
				}
			);
		};

		this.update = function(arrayMeetings) {
			var row, destcell, datecell;
			this.listcontainerbody.innerHTML = "";
			
			var self = this;
			arrayMeetings.forEach(function(meeting) {
				row = document.createElement("tr");
				destcell = document.createElement("td");
				destcell.textContent = meeting.title;
				row.appendChild(destcell);
				datecell = document.createElement("td");
				datecell.textContent = meeting.date;
				row.appendChild(datecell);
				datecell = document.createElement("td");
				datecell.textContent = meeting.time;
				row.appendChild(datecell);
				datecell = document.createElement("td");
				datecell.textContent = meeting.duration;
				row.appendChild(datecell);
				datecell = document.createElement("td");
				datecell.textContent = meeting.maxNumberOfParticipants;
				row.appendChild(datecell);
				datecell = document.createElement("td");
				datecell.textContent = meeting.createdBy;
				row.appendChild(datecell);

				self.listcontainerbody.appendChild(row);
			});

			this.listcontainer.style.visibility = "visible";

		}
	}


	function ShowAllUsers(_div_users, _alert) {
		this.divUsers = _div_users;
		this.alert = _alert;
		this.reset = function() {
			this.divUsers.style.visibility = "hidden";
		}

		this.show = function(next) {
			var self = this;
			makeCall("GET", "GetOtherUsers", null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							var usersToShow = JSON.parse(req.responseText);
							if (usersToShow.length == 0) {
								self.alert.textContent = "Non ci sono altri utenti";
								return;
							}
							self.update(usersToShow);
							if (next) next();  //show the default element of the list if present

						}

						else {
							self.alert.textContent = message;
						}
					}
				}
			);
		};

		this.update = function(arrayUsers) {
			var self = this;
			arrayUsers.forEach(function(user) {
				var checkbox = document.createElement('input');

				checkbox.type = "checkbox";
				checkbox.name = "checkbox[]";
				checkbox.value = user.username;
				checkbox.id = "users_checkbox";

				//creating label for checkbox
				var label = document.createElement('label');

				//assigning attributes for the created label tag
				label.htmlFor = "users_checkbox";

				//appending the created text to the created label tag
				label.appendChild(document.createTextNode(user.username));

				var br = document.createElement("br");
				var br2 = document.createElement("br");

				//appending the checkbox and label to div
				self.divUsers.appendChild(checkbox);
				self.divUsers.appendChild(label);
				self.divUsers.appendChild(br);
				self.divUsers.appendChild(br2);

			});

			this.divUsers.style.visibility = "visible";

		}
	}


	function SetTodayDate(formId) {
		var now = new Date(), formattedDate = now.toISOString().substring(0, 10);
		this.form = formId;
		this.form.querySelector('input[type="date"]').setAttribute("min", formattedDate);
	}

	function PageOrchestrator() {
		var alertMyMeetings = document.getElementById("alertMyMeetings");
		var alertOtherMeetings = document.getElementById("alertOtherMeetings");
		var alertForm = document.getElementById("alertForm");

		this.start = function() {
			
			welcomeMessage = new WelcomeMessage(sessionStorage.getItem('username'),
				document.getElementById("username_user"));
			welcomeMessage.show();

			myMeetingsList = new MyMeetingsList(
				alertMyMeetings,
				document.getElementById("myMeetingsContainer"),
				document.getElementById("myMeetingsBody"));

			otherMeetingsList = new OtherMeetingsList(
				alertOtherMeetings,
				document.getElementById("otherMeetingsContainer"),
				document.getElementById("otherMeetingsBody"));

			setTodayDate = new SetTodayDate(document.getElementById("createmeetingform"));

			document.querySelector("a[href='Logout']").addEventListener('click', () => {
	        	window.sessionStorage.removeItem('username');
	      	})

			this.refresh = function() {
				alertMyMeetings.textContent = "";
				alertOtherMeetings.textContent = "";
				alertForm.textContent = "";
				myMeetingsList.reset();
				myMeetingsList.show();
				otherMeetingsList.reset();
				otherMeetingsList.show();
			};
		}
	}

};
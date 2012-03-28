(function($) {
	$(document).ready(function() {
		$("a.add-torrent").click(function() {
			var self = this;
			toggleLoadingIcon(self);
			$("#add-torrent-dialog").load("/client/renderAddTorrent", function() {
				$(this).modal({
					"backdrop" : true
				});
				$(this).removeClass("hide");
				toggleLoadingIcon(self);
			});
		});
		
		$("body").on("click", "a.torrent-pause-button", function() {
			var hash = $(this).data("torrent-hash");
			toggleLoadingIcon(this);
			$.post("/client/pauseTorrent?ext=json", {
				"torrentHash" : hash
			}, function(data) {
				toggleLoadingIcon(this);
				if (data.success) {
					refreshTorrent(hash);
				} else {
					flashError(data.error);
				}
			})
		}).on("click", "a.torrent-resume-button", function() {
			var hash = $(this).data("torrent-hash");
			toggleLoadingIcon(this);
			$.post("/client/startTorrent?ext=json", {
				"torrentHash" : hash
			}, function(data) {
				toggleLoadingIcon(this);
				if (data.success) {
					refreshTorrent(hash);
				} else {
					flashError(data.error);
				}
			})
		}).on("click", "a.torrent-remove-button", function() {
			if (!confirm("Are you sure you want to remove this torrent file and all data?")) { return; }
			var hash = $(this).data("torrent-hash");
			var button = this;
			toggleLoadingIcon(button);
			$.post("/client/removeTorrent?ext=json", {
				"torrentHash" : hash,
				"torrentOnly" : false
			}, function(data) {
				toggleLoadingIcon(button);
				if (dataIsGood(data)) {
					$("#t_" + hash).fadeOut("slow");
				} else {
					alert("Data is not good!");
				}
			})
		}).on("click", "a.torrent-add-group", function() {
			$("<input class='torrent-add-group' type='text' style='width:80px;' name='group' placeholder='Type name'/>")
				.insertAfter(".container") //do it here so it can stick outside the container bounds 
				.position({
					"my" : "left",
					"at" : "left",
					"of" : this,
					"offset" : "40 0"
				})
				.focus()
				.data("torrent-hash", $(this).data("torrent-hash"));
			refreshingTorrentList = true;
			return false;
		}).on("keypress", "input.torrent-add-group", function(evt) {
			if (evt.keyCode == 13) {
				var group_name = $(this).val();
				var hash = $(this).data("torrent-hash");
				if (group_name) {
					$.post("/client/addTorrentGroup?ext=json", {
						"torrentHash" : hash,
						"group" : group_name
					}, function(data) {
						$("input.torrent-add-group").remove();
						refreshingTorrentList = false;
						if (dataIsGood(data)) {
							refreshTabs();
							refreshTorrent(hash);
						}
					});
				}
			}
		}).on("click", "a.torrent-remove-group", function() {
			var hash = $(this).data("torrent-hash");
			var self = this;
			$.post("/client/removeTorrentGroup?ext=json", {
				"torrentHash" : hash,
				"group" : $(this).siblings("span:first").text()
			}, function(data) {
				if (dataIsGood(data)) {
					$(self).parents("div.btn:first").fadeOut("slow");
					refreshTorrent(hash);
					refreshTabs();
				}
			})
		}).on("click", "#tab-header a", function() {
			var target = $(this).attr("href");
			activeGroup = target;
			refreshTorrentList();
		}).on("click", "a.torrent-files-button", function() {
			toggleLoadingIcon(this);
			var self = this;
			$.post("/client/renderTorrentInfo", {
				"torrentHash" : $(this).data("torrent-hash")
			}, function(data) {
				toggleLoadingIcon(self);
				$("body>.container").append(data);
				$("#torrent-info-modal").modal({
					"backdrop" : false
				});
			})
		}).on("click", "#remove-selected", function() {
			var hashes = [];
			$("tr.torrent input.torrent-checkbox:selected").each(function() {
				var hash = $(this).data("torrent-hash");
				hashes.push(hash);
				console.log("hash: " + hash);
			});
			console.log(hashes);
			$.post("/client/removeTorrents?ext=json", {
				"hashes" : hashes
			}, function(data) {
				if (data.success) {
					refreshingTorrentList = false;
					refreshTorrentList();
				} else {
					alert(data.error);
				}
			});
		}).on("click", "tr.torrent .torrent-checkbox", function() {
			var im_selected = $(this).is(":selected");
			console.log("im selected: " + im_selected);
			var selected = $("tr.torrent input.torrent-checkbox:selected").length;
			if (selected == 0 && im_selected) {
				selected = 1;
			}
			console.log("Selected: " + selected);
			refreshingTorrentList = (selected > 0)
		}).on("click", function() {
			//remove any open 'add group' inputs
			$("input.torrent-add-group").remove();
			$(".tooltip").tooltip("hide");
		});
		
		activeGroup = $("ul.torrent-nav-tabs li.active a").attr("href");
		refreshTorrentList();
	});
	
	var activeGroup;
	
	setInterval(function() {
		refreshTorrentList();
	}, "10000");
	
	var refreshingTorrentList = false;
	function refreshTorrentList(force) {
		if (force) { refreshingTorrentList = false; }
		if (!refreshingTorrentList) {
			refreshingTorrentList = true;
			$("#global-loader").show();
			var group = activeGroup.replace("#","");
			$(activeGroup).load("/client/renderTorrentList", { "group" : group },
			function(responseText) {
				checkForAuthResponse(responseText);
				refreshingTorrentList = false;
				$("#global-loader").hide();
			});
			//refresh stats too
			$("#user-stats").load("/client/userStats");
		}
	}
	window.refreshTorrentList = refreshTorrentList;
	
	function refreshTorrent(hash) {
		$.get("/client/renderTorrent", {
			"torrentHash" : hash
		}, function(data) {
			$("#t_" + hash).replaceWith(data);
		})
	}
	
	function flashError(message, title) {
		title = title || "An error has occured!";
		message = message || "";
		var alert = "<div class='dropdown-alert alert alert-error fade in hide' data-alert='alert'>" +
							"<a class='close' href='#' data-dismiss='alert'>&times;</a>" +
							"<h4 class='alert-heading'>" + title + "</h4>" +
							"<p class='alert-body'>" + message + "</p>" +
							"</div>"
		var a = $(alert);
		$(a).appendTo("#container").slideDown();
		setTimeout(function() {
			$(a).slideUp(function() { $(alert).remove(); });
		}, "5000");
	}
	
	function toggleLoadingIcon(element) {
		var icon = $(element).find("i:first");
		$(icon).toggleClass("icon-loader");
	}
	window.toggleLoadingIcon = toggleLoadingIcon;
	
	function checkForAuthResponse(text) {
		if (text.indexOf("<title>Log In</title>") != -1) {
			document.location.href = "/client/";
		}		
	}
	
	function dataIsGood(data) {
		if (!data.success) {
			flashError(data.error);
			return false;
		}
		return true;
	}
	
	function refreshTabs() {
		var active_tab = $("#tab-header").find("li.active a").attr("href");
		if (!active_tab) { active_tab = "#All"; }
		active_tab = active_tab.replace("#", "");
		$("#tab-header").load("/client/tabs", {
			"active" : active_tab
		});
	}
	
})(jQuery);


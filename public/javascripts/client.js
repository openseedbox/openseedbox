(function($) {
	
	window.refresh_disabled = true;
	
	//setup ajax to show global loader on every request
	$.ajaxSetup({
		beforeSend : function() {
			//$("#global-loader").show();
		},
		complete : function() {
			//$("#global-loader").hide();
		}
	});
	
	$(document).ajaxSuccess(function(evt, xhr) {
		var data = $.parseJSON(xhr.responseText);
		if (data.error) {
			$.flashError(data.error);
		}
	});
	
	$(window).on("focus", function() {
		window.refresh_disabled = false;
	}).on("blur", function() {
		window.refresh_disabled = true;
	});
	
	$(document).ready(function() {
		var already_refreshing = false;
		var activeGroup = "All";
		var activeAjaxRequest;
		var refresh = function(force) {
			if (already_refreshing && !force) {return;} //wait for previous refresh to finish before starting a new one, unless force is set
			if (window.refresh_disabled) {return;} //refresh gets disabled when window loses focus to take load off server
			already_refreshing = true;
			if (activeAjaxRequest && force) {
				activeAjaxRequest.abort();
			}
			activeAjaxRequest = $.getJSON("/client/update", {"group" : activeGroup, "ext" : "json"}, function(data) {
				
				if (!data.data) { return; }
				
				//hide any tooltips before replacing, otherwise bootstrap loses the reference to the tooltip text
				//and it will never disappear
				$(".torrent-add-group").tooltip("hide");
				
				//update torrent list
				$("#torrent-list").html(data.data["torrent-list"]);
				
				//update user stats
				$("#user-stats").html(data.data["user-stats"]);
				
				//update tabs incase user was adding/removing groups
				$("#tab-header").html(data.data["client-tabs"]);
				
				//process checked hashes
				$(".torrent-checkbox").each(function() {
					var my_hash = $(this).data("torrent-hash");
					if ($.inArray(my_hash, checked_hashes) != -1) {
						$(this).attr("checked", "checked");
					}
				});
				already_refreshing = false;
			});
		};
		refresh(); //load initial data
		setInterval(refresh, 2000);
		
		//NORMAL EVENT HANDLERS
		var do_action = function(action, callback) {
			if (checked_hashes.length > 0) {
				if (action == "removeTorrents") {
					if (!confirm("Are you sure you want to remove the " + checked_hashes.length + " selected torrents?")) { return; }
				}
				var group = "";
				if (action == "addTorrentGroups") {
					group = prompt("Please enter the name of the group to add these " + checked_hashes.length + " torrents to:");
					if (!group) { return; }
				}
				$("#group-actions-loader").show();
				window.refresh_disabled = true;
				$.post("/client/" + action + "?ext=json", {
					"torrentHashes" : checked_hashes,
					"group" : group
				}, function(data) {
					window.refresh_disabled = false;
					$("#group-actions-loader").hide();
					refresh();
					if (callback && $.isFunction(callback)) {
						callback();
					}
				});
			}			
		}
		$("#group-actions a.group-actions-start").click(function() {
			do_action("startTorrents");
		});
		$("#group-actions a.group-actions-pause").click(function() {
			do_action("pauseTorrents");
		});
		$("#group-actions a.group-actions-remove").click(function() {
			do_action("removeTorrents");
		});
		$("#group-actions a.group-actions-add-group").click(function() {
			do_action("addTorrentGroups");
		});		
		
		//LIVE EVENT HANDLERS
		var checked_hashes = []; //for persisting checkboxes between requests 
		
		var backend_message = "<h5>Querying backend, please be patient...</h5>";
		$("#torrent-list").html(backend_message);
		
		$("body").on("click", "#tab-header a", function() {
			activeGroup = $(this).text();
			$("#torrent-list").html(backend_message);
			checked_hashes = []; //reset checked hashes of changing tabs
			refresh(true);
		}).on("click", "a.torrent-pause-button", function() {
			var hash = $(this).data("torrent-hash");
			$(this).loading();
			$.post("/client/pauseTorrent?ext=json", {
				"torrentHash" : hash
			}, function(data) {
				if (data.error) {
					flashError(data.error);
				}
			})
		}).on("click", "a.torrent-resume-button", function() {
			var hash = $(this).data("torrent-hash");
			$(this).loading();
			$.post("/client/startTorrent?ext=json", {
				"torrentHash" : hash
			}, function(data) {
				if (data.error) {
					flashError(data.error);
				}
			});
		}).on("click", "a.torrent-remove-button", function() {
			var dialog = $("#remove-torrent-dialog");
			var hash = $(this).data("torrent-hash");
			var name = $(this).data("torrent-name");
			$("#remove-torrent-button").data("torrent-hash", hash);
			$(dialog).find(".torrent-name").html(name);
			$(dialog).modal("show");
			window.refresh_disabled = true;
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
			window.refresh_disabled = true;
			return false;
		}).on("keypress", "input.torrent-add-group", function(evt) {
			if (evt.keyCode == 13) {
				var group_name = $(this).val();
				var hash = $(this).data("torrent-hash");
				if (group_name) {
					$.post("/client/addTorrentGroup?ext=json", {
						"torrentHash" : hash,
						"group" : group_name
					}, function() {
						$("input.torrent-add-group").remove(); //for some reason, doing the self/this thing doesnt work
						window.refresh_disabled = false;
						refresh();
					});
				}
			}
		}).on("click", "a.torrent-remove-group", function() {
			var hash = $(this).data("torrent-hash");
			var self = this;
			$.post("/client/removeTorrentGroup?ext=json", {
				"torrentHash" : hash,
				"group" : $(this).siblings("span:first").text()
			}, function() {
				$(self).parents("div.btn:first").fadeOut("slow");
				refresh();
			})
		}).on("click", "a.torrent-info-button", function() {
			var self = this;
			$(self).loading();
			$.post("/client/renderTorrentInfo", {
				"torrentHash" : $(this).data("torrent-hash")
			}, function(data) {
				$(self).loading();
				$("body>.container").append(data);
				$("#torrent-info-modal").modal({
					"backdrop" : false
				});
			})
		}).on("click", ".torrent-checkbox", function() {
			var hash = $(this).data("torrent-hash");
			if ($(this).is(":checked")) {
				checked_hashes.push(hash);
			} else {
				var idx = $.inArray(hash, checked_hashes);
				if (idx != -1) {
					checked_hashes = checked_hashes.slice(idx + 1);
				}
			}
		});	
	});
})(jQuery);

/*
 * Loading Icon plugin - replaces the first icon it finds in the element with an ajax loader
 * Flash Error plugin - easy way to flash error messages to the user
 */
(function($) {
	$.fn.loading = function() {
		return this.each(function() {
			var icon = $(this).find("i:first");
			$(icon).toggleClass("icon-loader");			
		});
	};
	$.extend({
		flashError : function(error) {
			$("#flash-error-content").html(error);
			$("#flash-error").show();
		}
	});
})(jQuery);
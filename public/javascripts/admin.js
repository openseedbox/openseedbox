(function($) {
	$(document).ready(function() {
		$("#tab-content")
		.on("click", "a.start-transmission", function() {
			var id = $(this).data("node-id");
			setLoading(this);
			$.post("/transmission/start", {
				"ext" : "json", "id" : id
			}, function() {
				reloadRow(id);
			});
		}).on("click", "a.stop-transmission", function() {
			var id = $(this).data("node-id");
			setLoading(this);
			$.post("/transmission/stop", {
				"ext" : "json", "id" : id
			}, function() {
				reloadRow(id);
			});			
		}).on("click", "a.restart-transmission", function() {
			var id = $(this).data("node-id");
			setLoading(this);
			$.post("/transmission/restart", {
				"ext" : "json", "id" : id
			}, function() {
				reloadRow(id);
			});			
		}).on("click", "a.edit-transmission-config", function() {
			var id = $(this).data("node-id");
			var self = this;
			setLoading(self);
			$.post("/admin/renderEditConfigDialog", {"id" : id}, function(data) {
				clearLoading(self);
				$(data).appendTo("#container").modal();
			});
		});
		
		$("#container").on("click", "#edit-config .save-button", function() {
			var d = $("#edit-config-form").serialize();
			$.post("/admin/updateNodeConfig?ext=json", d, function(data) {
				if (data.success) {
					$("#edit-config").modal("hide").remove();
				} else {
					alert("Error: " + data.error);
				}
			})
			return false;
		}).on("click", "#edit-config .set-default-button", function() {
			var id = $(this).data("node-id");
			$.post("/transmission/setDefaultConfig?ext=json", { "id" : id }, function(data) {
				if (data.success) {
					$("#edit-config").modal("hide").remove();
				} else {
					alert(data.error);
				}
			});
		});
		
		function reloadRow(node_id) {
			var row = $("tr.node-" + node_id);
			$.post("/admin/renderNodeRow", { "id" : node_id }, function (data) {
				$(row).replaceWith(data);
			});
		}
		
		function setLoading(elem) {
			$(elem).siblings(".ajax-loader").show();
		}
		
		function clearLoading(elem) {
			$(elem).siblings(".ajax-loader").hide();
		}
	});
})(jQuery);

//USERS TAB

(function($) {
	$("#container").on("click", ".edit-user", function() {
		
	});
})(jQuery);



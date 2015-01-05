
var default_table = false;
var default_log = false;
var default_graph = true;
var default_data = false;

var table_event = function(el) {
	if (el.checked == true) {
		$("table").show();
	} else {
		$("table").hide();
	}
	return;
}

var log_event = function(el) {
	if (el.checked == true) {
		$(".testing").show();
		$("br").show();
		$(".step").show();
	} else {
		$(".testing").hide();
		$("br").hide();
		$(".step").hide();
	}
	return;
}


var graph_event = function(el) {
	if (el.checked == true) {
		$("img").show();
	} else {
		$("img").hide();
	}
	return;
}

var data_event = function(el) {
	if (el.checked == true) {
		$(".data").show();
		$(".info").show();
		$(".transition").show();
	} else {
		$(".data").hide();
		$(".info").hide();
		$(".transition").hide();
	}
	return;
}

$(document).ready(function() {
	/* Initialisation according to default values */
	if (default_table) {
		$("#showTables").trigger("click");
	}
	if (default_log) {
		$("#showLogs").trigger("click");
	}
	if (default_graph) {
		$("#showGraphs").trigger("click");
	}
	if (default_data) {
		$("#showData").trigger("click");
	}
});

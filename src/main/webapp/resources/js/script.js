$(function() {
	var form = $("#form-search"),
		startBtn = $("#start-button"),
		stopBtn = $("#stop-button"),
		twDataWrapper = $("#twdata"),
		websocketSession = null,
		data = [],
	    totalPoints = 50, 
	    i = 0,
	    stop = false,
	    ymax = 30;
	
	var options = {
			series: {
				shadowSize: 0	// Drawing is faster without shadows
			},
			yaxis: {
				min: 0,
				max: ymax,
				tickFormatter: function(val, axis) { return val < axis.max ? val.toFixed(0) : "Msg/sec";}
			},
			xaxis: {
				show: true
			}
		};
	
	open();
	dataPrepare();
	
	function dataPrepare() {
		data = [];
		for (i = 0; i < totalPoints; i++) {
			data.push([i, 0]);
		}
		
		// clear tweets
		twDataWrapper.html("");
	}
	
	
	
	var plot = $.plot("#placeholder", [data], options);
	
	startBtn.click(function(e) {
		submitTerm(e);
	});
	
	form.submit(function(e) {
		submitTerm(e);
	});
	
	stopBtn.click(function(e) {
		if (websocketSession) {
			e.preventDefault();
			websocketSession.send("stop");
			$(this).attr("disabled", "disabled");
			dataPrepare();
			startBtn.button("reset");
			stop = true;
			//websocketSession.close();
			//websocketSession = null;
		}
	});
	
	function submitTerm(e) {
		e.preventDefault();
		open();
		websocketSession.send($("#term").val());
		startBtn.button("loading");
		stop = false;
	}
	
	function fOnMessage(evt) {
		var twData = JSON.parse(evt.data);
		var twDataString = "";
		//console.log(twData);
		if (twData == "stream is already in use") {
			startBtn.attr("disabled", "disabled");

		}
		else if (twData == "stream is ready") {
			startBtn.removeAttr("disabled");
		}
		else {
			// release submit button
			if (stopBtn.is(":disabled") && !stop) {
				stopBtn.removeAttr("disabled");
				startBtn.attr("disabled", "disabled");
			}
			
			
			var res = getProcessedData(twData);
			
			// activate plot
			plot.setData([res]);
			//plot.setupGrid();
			plot.draw();
			
			
			// fill twdata div
			for (var i in twData) {
				twDataString += "<p>" + twData[i] + "</p>";
			}
			twDataWrapper.prepend(twDataString);
		}
	}
	
	function getProcessedData(twData) {
		if (data.length > 0) {
			data = data.slice(1);
		}
		
		while (data.length < totalPoints) {
			var twDataLength = twData.length;
			data.push(twDataLength);
		}
		
		var res = [];
		for (var i = 0; i < data.length; ++i) {
			res.push([i, data[i]]);
		}
		return res;
	}
	
	function open() {
	    if (!websocketSession) {
	        websocketSession = new WebSocket('ws://' + document.location.host + document.location.pathname +'twstream');
	        websocketSession.onmessage = fOnMessage;
	    }
	}
});
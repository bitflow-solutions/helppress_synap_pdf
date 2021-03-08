$(function () {
	$("#btn-download-all").click(function() {
		downloadAll();
	});
	$("#btn-download-changed").click(function() {
		downloadChanged();
	});
	$("#check-all-file").click(function() {
		// console.log('checked ' + ($(this).prop("checked")==true));
		if ($(this).prop("checked")==true) {
			$(".changed-files:enabled").prop("checked", true);
		} else {
			$(".changed-files:enabled").prop("checked", false);
		}
	});
	initSocket();
});

function downloadFromHistory(id) {
	confirm("해당 버전을 다운로드 하시겠습니까?", function(ret) {
		$("#snackbar").html("").foundation('close');
		$(".spinner").show();
		$("#ifrm").attr("src", "/api/v1/ecm/release/all/" + id);
//		setTimeout(function() {
//			$(".spinner").hide();
//		}, 1000);
	});
}

function downloadAll() {
	var release = false;
	confirm("현재 버전을 배포 처리로 기록하시겠습니까?", function(ret) {
		$("#snackbar").html("").foundation('close');
		release = ret;
		$(".spinner").show();
		$("#ifrm").attr("src", "/api/v1/ecm/release/all?release=" + release);
//		setTimeout(function() {
//			$(".spinner").hide();
//		}, 12000);
	});
}

function downloadChanged() {
	var release = false;
	var checkboxes = document.querySelectorAll('input[name="fileIds"]:checked');
	if (checkboxes.length<1) {
		alert("파일을 하나 이상 선택해주세요!");
		return;
	}
	var fileIds = new Array();
	for (var i=0; i<checkboxes.length; i++) {
		var checkbox = checkboxes[i];
		fileIds.push(checkbox.value);
	}
	confirm('선택 파일들을 배포 처리 하시겠습니까?<br>"예"를 누르시면 변경파일에서 제외됩니다.', function(ret) {
		$("#snackbar").html("").foundation('close');
		release = ret;
		alert('다운로드에 몇 초 소요됩니다');
		$(".spinner").show();
		document.getElementById('ifrm').src = "/api/v1/ecm/release/changed?release=" + release + "&fileIds=" + fileIds;
    });
}

function initSocket() {
	var socket = new SockJS('ws');
	var stompClient = Stomp.over(socket);
	stompClient.debug = null;
	stompClient.connect({}, function (frame) {
	    console.log("connected");
        stompClient.subscribe('/download', function (msg) {
		  var shouldreload = JSON.parse(msg.body);
          console.log("message: " + shouldreload);
		  $(".spinner").hide();
		  if (shouldreload) {
            location.reload();
		  }
        });
    });
}

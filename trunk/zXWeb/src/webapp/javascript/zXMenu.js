//----
// What		:	zXMenu.js
// Who		:	Bertus Dispa
// When		:	8SEP02
// Why		:	zX menu functions
//
// Change	:	MB01NOV04
// Why		:	Added in browser independence code.
//
// Change	:	v1.5:95 DGS26APR2006
// Why		:	In zXSubMMenuToggle, a new technique for toggling menu images allows 
//			non-standard images to be used
//
//----

//==============================
// menuFocus
// _img - Image associated with menu
// _mnu - Div that implements the menu
//
// Open / close menu
//==============================

function zXSubMMenuToggle(_img, _mnu)
{
	var strImgNme;

	strImgNme = _img.src;
	
    //identify the element based on browser type
    if (document.layers) {
        objElement = document.layers[_mnu];
    } else if (document.all) {
        objElement = document.all[_mnu];
    } else if (document.getElementById) {
        objElement = document.getElementById(_mnu);
    }
    
    if(isNS4 || isIE4){
        if (objElement.style.visibility ="hidden") {
            objElement.style.visibility ="visible";
            //_img.src = imgDir + "menuOpen.gif";
			try {
				eval("strImgNme = "+col[i].id+"_OPEN");
			} catch(e) {
				strImgNme = "../images/menuOpen.gif";
			}
			
        } else {
            objElement.style.visibility ="hidden";
			//_img.src = "../images/menuClosed.gif";
			try {
				eval("strImgNme = "+col[i].id+"_CLOSED");
			} catch(e) {
				strImgNme = "../images/menuClosed.gif";
			}
        }
        
    } else { 
        if (objElement.style.display == "none") {
            objElement.style.display = "" ;
            //_img.src = imgDir + "menuOpen.gif";
			try {
				eval("strImgNme = "+col[i].id+"_OPEN");
			} catch(e) {
				strImgNme = "../images/menuOpen.gif";
			}
        } else {
            objElement.style.display = "none" ;
			//_img.src = "../images/menuClosed.gif";
			try {
				eval("strImgNme = "+col[i].id+"_CLOSED");
			} catch(e) {
				strImgNme = "../images/menuClosed.gif";
			}
        }
    }
    _img.src = strImgNme;
}

//==============================
// zXMMGoToPage
// _url - Url to go to
//
// Go somewhere from the main menu
//==============================

function zXMMGoToPage(_url) 
{
	if (parent.fraDetails1) {
		parent.fraDetails1.location = _url;
	
		parent.document.getElementsByTagName('frameset')[2].setAttribute('rows','0,*,0,0,0,0');
	
		// IE4+ hide scroll bar
		parent.fraDetailsMenu.document.body.style.overflow='hidden';

	} else {
		window.location = _url;
	}
}

//<------------------------------------------------------------------------ DO NOT MERGE
// -------------------------------------------------- Alpha code, will need to rewrite.
function menu_autocomplete(_session, id, popupId) {
	var isSafari;
	var isMoz;
	var isIE;
	if (navigator.userAgent.indexOf("Safari") > 0) {
		isSafari = true;
		isMoz = false;
		isIE = false;
	} else if (navigator.product == "Gecko") {
		isSafari = false;
		isMoz = true;
		isIE = false;
	} else {
		isSafari = false;
		isMoz = false;
		isIE = true;
	}
	
    var inputField = document.getElementById(id);
    var popup = document.getElementById(popupId);
    var current = 0;
    
	function constructUri() {
		var strURL = "../" + SCRPT + "/zXMainMenu." + SCRPT + "?";
		strURL += "-s=" + _session; // Session
		strURL += "&-a=seachmenu";
		strURL += "&-qury=" + escape(inputField.value); // The value to search on
		strURL += "&" + new Date().getTime();
	    return strURL;
	}
    
	function handleClick(e) {
		popup.style.visibility = 'hidden';
	}
    
	function handleOver(e) {
		popup.firstChild.childNodes[current].className = '';
		current = eventElement(e).index;
		popup.firstChild.childNodes[current].className = 'selected';
	}
    
    function post() {
        current = 0;
		var options = popup.firstChild.childNodes;
		
		var doPopup = false;
		if ((options.length > 1) || (options.length == 1 && options[0].innerHTML != inputField.value)) {
			if (options.length == 1 && options[0].innerHTML == inputField.value + " [" + options[0].id + "]") {
					doPopup = false;
			} else {
				doPopup = true;
			}
		}
		
        if(doPopup) {
			setPopupStyles();
			for(var i = 0; i < options.length; i++) {
				options[i].index = i;
				addOptionHandlers(options[i]);
			}
			options[0].className = 'selected';
			
        } else {
        	// Auto select
        	if (options.length == 1 && options[0].innerHTML == inputField.value) {
        		// Abusing alt tag.
			    inputField.alt = options[0].title;
        	}
			popup.style.visibility = 'hidden';
        }
    }
	
	function setPopupStyles() {
    	var maxHeight
    	
		if(isIE) {
			maxHeight = 200;
		} else {
		    maxHeight = window.outerHeight/3;
		}
		
		if(popup.offsetHeight < maxHeight) {
			popup.style.overflow = 'hidden';
		} else if(isMoz) {
		    popup.style.maxHeight = maxHeight + 'px';
			popup.style.overflow = '-moz-scrollbars-vertical';
		} else {
			popup.style.height = maxHeight + 'px';
			popup.style.overflowY = 'auto';
		}
		
		popup.scrollTop = 0;
		popup.style.visibility = 'visible';
	}
    
	function addOptionHandlers(option) {
		if(isMoz) {
			option.addEventListener("mouseover", handleOver, false);
			option.addEventListener("click", handleClick, false);
		} else {
			option.attachEvent("onmouseover", handleOver, false);
			option.attachEvent("onclick", handleClick, false);
		}
	}
	
    var updater = liveUpdater(constructUri, post);
    
    //==============
	//liveUpdater
	//
	//uriFunc = Function to get the url to post to.
	//postFunc = Function to call once we have processed the xmlhttp request.
	//==============
	function liveUpdater(uriFunc, postFunc) {
	    var request = new XMLHttpRequest();
	    function update() {
	        if(request && request.readyState < 4) {
	        	request.abort();
	        }
	        request = new XMLHttpRequest();
	        
	        request.onreadystatechange = processRequestChange;
	        request.open("GET", uriFunc());
			if (window.ActiveXObject) { 
				request.send();
			} else {
				request.send(null);
			}
	        return false;
	    }
	    
	    function processRequestChange() {
	        if(request.readyState == 4) {
		        if (request.status == 200) {
		        	if (request.responseText) {
		                popup.innerHTML = request.responseText; 
		                postFunc();
	                }
	            }
	        }
	    }
	    
	    return update;
	}
	
    var timeout = false;
   	
	function start(e) {
		if (timeout) window.clearTimeout(timeout);
		
		//up arrow
		if(e.keyCode == 38) {
			if(current > 0) {
				popup.firstChild.childNodes[current].className = '';
				current--;
				popup.firstChild.childNodes[current].className = 'selected';
				popup.firstChild.childNodes[current].scrollIntoView(true);
			}
		}
		//down arrow
		else if(e.keyCode == 40) {
			if(current < popup.firstChild.childNodes.length - 1) {
				popup.firstChild.childNodes[current].className = '';
				current++;
				popup.firstChild.childNodes[current].className = 'selected';
				popup.firstChild.childNodes[current].scrollIntoView(false);
			}
		}
		//enter or tab or escape
		else if((e.keyCode == 13 || e.keyCode == 9) && popup.style.visibility == 'visible') {
		    inputField.value = popup.firstChild.childNodes[current].innerHTML;
		    
		    popup.style.visibility = 'hidden';
		    inputField.focus();
		    if(isIE) {
				event.returnValue = false;
		    } else {
				e.preventDefault();
		    }
		    
		    // Abusing alt tag.
		    inputField.alt = popup.firstChild.childNodes[current].title;
			eval(popup.firstChild.childNodes[current].title);
			
		// enter - If alt tag is set	
		} else if (e.keyCode == 13) {
			if (inputField.alt != undefined && inputField.alt != "") {
					// Abusing alt tag.
					eval(inputField.alt);
			}
			
		// ESC - Cleanup
		} else if (e.keyCode == 27) {
			inputField.alt = "";
			inputField.value = "";
			popup.style.visibility = 'hidden';
			
		} else {
			timeout = window.setTimeout(updater, 50);
		}
	}
	
	if (inputField.focus) {
		inputField.focus();
	}
	
	// Event traps.
	addKeyListener(inputField, start);
	// Could not select items properly
	//addBlurListener(inputField, function() {popup.style.visibility = 'hidden';});
	addClickListener(document, function() {popup.style.visibility = 'hidden';});
	
	/* Functions to handle browser incompatibilites */
	function eventElement(event) {
		if(isMoz) {
			return event.currentTarget;
		} else {
			return event.srcElement;
		}
	}
	
	function addClickListener(element, listener) {
		if (isMoz) {
			element.addEventListener('click', listener, true);
		} else {
			element.attachEvent('onclick', listener);
		}
	}
	
	function addKeyListener(element, listener) {
		if (isSafari) {
			element.addEventListener("keydown", listener, false);
		} else if (isMoz) {
			element.addEventListener("keypress", listener, false);
		} else {
			element.attachEvent("onkeydown", listener);
		}
	}
	
	function addBlurListener(element, listener) {
		if(isIE) {
			element.attachEvent("onblur", listener);
		} else {
			element.addEventListener("blur", listener, false);
		}
	}
}
//DO NOT MERGE ------------------------------------------------------------------------>
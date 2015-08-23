//----
// What		:	zXTree.js
// Who		:	Bertus Dispa
// When		:	17SEP02
// Why		:	zX tree  functions
//----

//----
// Variable used to maintain state of tree
//----
var zXTreeOpen = 0;

//
//zXTreeLevelToggle
// User has clicked on a treeform level
//
// _img - Image associated with level
// _level - Name of the div
//
function zXTreeLevelToggle(_img, _level)
{
	if (document.layers) {
        objLevel = document.layers[_level];
    } else if (document.all) {
        objLevel = document.all[_level];
    } else if (document.getElementById) {
        objLevel = document.getElementById(_level);
    }
    
	if ( objLevel.style.display == "none" )
	{
		objLevel.style.display = "" ;
		_img.src = imgDir + "menuOpen.gif";
	}
	else
	{
		objLevel.style.display = "none" ;
		_img.src = imgDir + "menuClosed.gif";
	}

	return false;
}			

//----
// zXToggleWholeTree
//		Open / close whole account group tree
//
// _img - Handle to toggle image
//----
function zXToggleWholeTree(_img)
{
	var i;
	var col;
	
	if (isIE4) 
	{
		col = document.all.tags("DIV");
	} 
	else 
	{
		col = document.getElementsByTagName("DIV");
	}
	
	if (col != null) 
	{
	    for (i = 0; i < col.length; i++) 
		{
			if (col[i].id.match("_tree_") )
			{
				if (zXTreeOpen == 1)
				{
					col[i].style.display="none";
				}
				else
				{
					col[i].style.display="";
				}
			}
		}
	}
	
	if (isIE4) 
	{
		col = document.all.tags("IMG");
	} 
	else 
	{
		col = document.getElementsByTagName("IMG");
	}
	if (col != null) 
	{
	    for (i = 0; i < col.length; i++) 
		{
			if (col[i].id.match("_treeImg_") )
			{
				if (zXTreeOpen == 1)
				{
					col[i].src = imgDir + "menuClosed.gif";
				}
				else
				{
					col[i].src = imgDir + "menuOpen.gif";
				}
			}
		}
	}

	if (zXTreeOpen == 1)
	{
		zXTreeOpen = 0;
		_img.src = imgDir + "menuClosed.gif";
	}
	else
	{
		zXTreeOpen = 1;
		_img.src = imgDir + "menuOpen.gif";
	}
}

from aqt.utils import showInfo
from aqt import mw
from anki.hooks import addHook
from anki.utils import checksum
from subprocess import call
import re, os, shutil, time

dp_mypath = os.path.dirname(os.path.abspath(__file__))
dp_fname = None;
dp_drawing = None;
dp_editor = None;

def drawingPad(ed):
    global dp_editor
    global dp_drawing
    global dp_fname

    dp_editor = ed
    dp_fname = "drawingPad-"+checksum(str(time.time()))+".png"
    dp_drawing = os.path.join(dp_mypath, dp_fname)
    dp_editor.web.evalWithCallback("window.getSelection().getRangeAt(0).cloneContents().querySelector('img').src", getSelectionCallback)

def getSelectionCallback(imageSrc):

    if imageSrc == None:
        call(["java", "-jar", os.path.join(dp_mypath, "drawingPad.jar"), dp_drawing])
    else:
        imageSrc = imageSrc.split(os.sep).pop()
        call(["java", "-jar", os.path.join(dp_mypath, "drawingPad.jar"), dp_drawing, "-i", imageSrc])

    if os.path.exists(dp_drawing):
        mdir = mw.col.media.dir()
        shutil.copyfile(dp_drawing, os.path.join(mdir, dp_fname))
        dp_editor.web.eval("wrap(\"<img src='"+dp_fname+"' />\", \"\");")
        os.remove(dp_drawing)
    else:
        dp_editor.web.eval("wrap(\"Unable to start DrawingPad. Do you have JavaFx installed?\", \"\");")

def addMyButton(buttons, editor):
    editor._links['drawingPad'] = drawingPad
    return buttons + [editor._addButton(
        os.path.join(dp_mypath, "drawingPad.png"),
        "drawingPad",
        "Drawing Pad")]

addHook("setupEditorButtons", addMyButton)

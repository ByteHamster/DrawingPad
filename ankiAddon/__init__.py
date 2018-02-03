from aqt.utils import showInfo
from aqt import mw
from anki.hooks import addHook
from anki.utils import checksum
from subprocess import call
import re, os, shutil, time

mypath = os.path.dirname(os.path.abspath(__file__))

def drawingPad(editor):
    fname = "drawingPad-"+checksum(str(time.time()))+".png"
    drawing = os.path.join(mypath, fname)
    call(["java", "-jar", os.path.join(mypath, "drawingPad.jar"), drawing])
    
    if os.path.exists(drawing):
        mdir = mw.col.media.dir()
        shutil.copyfile(drawing, os.path.join(mdir, fname))
        editor.web.eval("wrap(\"<img src='"+fname+"' />\", \"\");")
        os.remove(drawing)
    else:
        editor.web.eval("wrap(\"Unable to start DrawingPad. Do you have JavaFx installed?\", \"\");")

def addMyButton(buttons, editor):
    editor._links['drawingPad'] = drawingPad
    return buttons + [editor._addButton(
        os.path.join(mypath, "drawingPad.png"),
        "drawingPad",
        "Drawing Pad")]

addHook("setupEditorButtons", addMyButton)

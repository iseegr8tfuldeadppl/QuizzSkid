
import pytesseract
from PIL import Image
import os
import pyperclip

def copy2clip(txt):
    pyperclip.copy(txt)


#https://www.pyimagesearch.com/2017/07/10/using-tesseract-ocr-python/
text = pytesseract.image_to_string(Image.open("C:\\Users\\BenBouali\\Desktop\\Screenshot_1.png"), lang="fra")
copy2clip(text)
print("copied!")
os.remove("C:\\Users\\BenBouali\\Desktop\\Screenshot_1.png")

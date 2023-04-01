import json

with open("answers.json", 'rb') as f:
    answers = json.load(f)
    with open("cleaned_answers.txt", "wb") as f2:
        index = 0
        for question in answers["_embedded"]["questions"]:
            f2.write(("Question " + str(index+1)).encode())
            f2.write('\n'.encode())
            f2.write(("Q: " + question["content"]).encode())
            f2.write('\n'.encode())
            f2.write(("A: " +  question["choices"][question["correctAnswer"]]).encode())
            f2.write('\n'.encode())
            f2.write('\n'.encode())
            index += 1
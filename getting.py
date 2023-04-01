import requests

headers = {
    "Content-Type": "application/json",
}

data = {
    "skipJokers": 5
}

response = requests.get("https://quizzing.samiratv.net/users/efaf5074-afa0-4049-80f2-d6815d146ac4", data=data, headers=headers)

print(response.text)
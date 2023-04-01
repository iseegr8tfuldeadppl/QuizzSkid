import requests

#https://quizzing.samiratv.net/users/search?invitationCode=4hgoo9VL
#&id=ac5ee842-fc57-4588-a6c8-bd22489a979e
# try using http instead of https in the url
# methods: patch, get, post, delete, put
response = requests.patch("https://quizzing.samiratv.net/users/search/updateJokersToAll?fiftyJokers=1&skipJokers=1")

print(response.text)
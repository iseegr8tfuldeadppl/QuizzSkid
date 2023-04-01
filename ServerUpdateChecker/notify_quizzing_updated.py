import requests
import time
import playsound #if playsound doesn't work then try this: pip install playsound==1.2.2
import keyboard
import json

ping_period = 1*60 # pings server every 10 minutes
notify_sound_period = 2 # in seconds

# shared between threads
sounds_to_make = 3 # user can't cancel before at least 2 sound notifications
sounds_made = 0
notifying = False
esc_clicked = False


#def button_clicks_detector():
#    global notifying, esc_clicked
#    print("Stopping: " + str(sounds_made) + "/" + str(sounds_to_make))
#    esc_clicked = True

# Debugging
import time
simulated_notifications_period = 3 # seconds

with open("server_content.json", "r") as f:
    server_content = json.load(f)
    print("Loaded file")
    f.close()



def main():
    global sounds_made, server_content
    global notifying, esc_clicked
    last_ping = 0
    last_notify_sound = 0
    while True:
        try:
            esc_clicked = esc_clicked or keyboard.is_pressed("esc")

            if notifying:

                if esc_clicked and sounds_made >= sounds_to_make:
                    notifying = False
                    esc_clicked = False
                    sounds_made = 0
                else:
                    if time.time() - last_notify_sound >= notify_sound_period:
                        playsound.playsound('et-voila-message-tone.mp3', True)
                        last_notify_sound = time.time()
                        sounds_made += 1

            else:
                if time.time() - last_ping >= ping_period or last_ping == 0:
                    print("Pinging..")
                    response = requests.get("http://quizzing.samiratv.net/quizzes")

                    if server_content != response.json():
                        print("Server update detected")
                        notifying = True
                    else:
                        last_ping = time.time()
                        print("Going to wait 1 minute")
                        time.sleep(ping_period)

        except Exception as e:
            print("Error in main loop", e)
            last_ping = time.time()
            time.sleep(ping_period)

if __name__ == '__main__':
    #keyboard.add_hotkey("esc", button_clicks_detector)
    main()
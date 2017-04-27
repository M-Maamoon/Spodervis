from pubnub.pnconfiguration import PNConfiguration
from pubnub.pubnub import PubNub
import pygame

from pubnub.callbacks import SubscribeCallback
from pubnub.enums import PNOperationType, PNStatusCategory
import RPi.GPIO as GPIO
import threading

GPIO.setmode(GPIO.BOARD)
GPIO.setup(11, GPIO.OUT)
GPIO.setup(12, GPIO.IN)

pnconfig = PNConfiguration()
pnconfig.subscribe_key = "sub-c-6e10bdfe-1ad0-11e7-aca9-02ee2ddab7fe"
pnconfig.publish_key = "pub-c-bcba6aa9-1ae4-4658-bfbd-ab52df9adb44"
pnconfig.ssl = False
 
pubnub = PubNub(pnconfig)

def publish_callback(envelope, status):
    # Check whether request successfully completed or not
    if not status.is_error():
        pass  # Message successfully published to specified channel.
    else:
        pass  # Handle message publish error. Check 'category' property to find out possible issue
        # because of which request did fail.
        # Request can be resent using: [status retry];
 
 
class MySubscribeCallback(SubscribeCallback):
    def status(self, pubnub, status):
        pass
        if status.operation == PNOperationType.PNSubscribeOperation \
                or status.operation == PNOperationType.PNUnsubscribeOperation:
            if status.category == PNStatusCategory.PNConnectedCategory:
                pass
                # This is expected for a subscribe, this means there is no error or issue whatsoever
            elif status.category == PNStatusCategory.PNReconnectedCategory:
                pass
                # This usually occurs if subscribe temporarily fails but reconnects. This means
                # there was an error but there is no longer any issue
            elif status.category == PNStatusCategory.PNDisconnectedCategory:
                pass
                # This is the expected category for an unsubscribe. This means there
                # was no error in unsubscribing from everything
            elif status.category == PNStatusCategory.PNUnexpectedDisconnectCategory:
                pass
                # This is usually an issue with the internet connection, this is an error, handle
                # appropriately retry will be called automatically
            elif status.category == PNStatusCategory.PNAccessDeniedCategory:
                pass
                # This means that PAM does allow this client to subscribe to this
                # channel and channel group configuration. This is another explicit error
            else:
                pass
                # This is usually an issue with the internet connection, this is an error, handle appropriately
                # retry will be called automatically
        elif status.operation == PNOperationType.PNSubscribeOperation:
            # Heartbeat operations can in fact have errors, so it is important to check first for an error.
            # For more information on how to configure heartbeat notifications through the status
            # PNObjectEventListener callback, consult <link to the PNCONFIGURATION heartbeart config>
            if status.is_error():
                pass
                # There was an error with the heartbeat operation, handle here
            else:
                pass
                # Heartbeat operation was successful
        else:
            pass
            # Encountered unknown status type
 
    def presence(self, pubnub, presence):
        pass  # handle incoming presence data
 
    def message(self, pubnub, message):
		if message.message[0] ==  'light_on':
			GPIO.output(11, GPIO.HIGH)
		if message.message[0] ==  'light_off':
			GPIO.output(11, GPIO.LOW)
		if message.message[0] ==  'music_on':
			playMusic()
		if message.message[0] ==  'music_off':
			stopMusic()
	
		print message.message
		
def playMusic():
	pygame.mixer.init()
	pygame.mixer.music.load("lost.mp3")
	pygame.mixer.music.play()

def stopMusic():
	pygame.mixer.music.stop()


def sensorDataReader():
    value = GPIO.input(12)
    print value
    pubnub.publish().channel('sensor_data').message(['light', value]).async(publish_callback)
    threading.Timer(60, sensorDataReader).start()



sensorDataReader()	 
pubnub.add_listener(MySubscribeCallback())
pubnub.subscribe().channels('commands').execute()

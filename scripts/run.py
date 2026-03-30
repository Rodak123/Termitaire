import sys
import os
import shutil
from utils.run_command import run_command

def dev_action():
    print('Starting app...')

    run_command("gradle jar -q")

    if not os.path.exists(JAR_PATH):
        raise Exception(f"JAR not found at: '{JAR_PATH}'")
    
    run_command(f"java -jar {JAR_PATH}")

def build_action():
    print('Starting build...')

    run_command('gradle jar')

    if not os.path.exists(DIST_PATH):
        os.makedirs(DIST_PATH)
    
    shutil.copy2(JAR_PATH, DIST_PATH)
    print(f"Copied into '{DIST_PATH}'")

    print('Build finished')

def main():
    if len(sys.argv) < 2:
        print(USAGE)
        return

    action_key = sys.argv[1].lower()
    
    if not action_key in actions.keys():
        print(f"Invalid action: '{action_key}'")
        return
    
    action = actions[action_key]

    try:
        action()
    except Exception as e:
        print(f"Failed to execute action '{action_key}': {e}")

actions = {
    'dev': dev_action,
    'build': build_action
}

APP = 'Termitaire-1.0.jar'
DIST_DIR = 'dist'

JAR_PATH = os.path.join('build', 'libs', APP)
DIST_PATH = os.path.join(DIST_DIR, APP)

BASE_NAME = os.path.basename(__file__)
USAGE = f"Usage: python {os.path.basename(__file__)} [{'|'.join(actions.keys())}]"

if __name__ == "__main__":
    main()

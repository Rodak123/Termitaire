import subprocess

def run_command(command, shell=True):
    """Utility for running system commands"""
    subprocess.run(command, shell=shell, check=True)
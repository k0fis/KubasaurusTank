#!/usr/bin/env python3
"""Generate chiptune music tracks for KubasaurusTank using wave module."""
import wave
import struct
import math
import os
import random

SAMPLE_RATE = 22050
OUTPUT_DIR = "assets/music"

def write_wav(filename, samples, sample_rate=SAMPLE_RATE):
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    path = os.path.join(OUTPUT_DIR, filename)
    with wave.open(path, 'w') as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(sample_rate)
        for s in samples:
            s = max(-1.0, min(1.0, s))
            f.writeframes(struct.pack('<h', int(s * 32767)))
    print(f"  Generated: {path} ({len(samples)/sample_rate:.1f}s)")

def square(freq, t, volume=0.3):
    if freq == 0: return 0
    return volume * (1 if math.sin(2 * math.pi * freq * t) > 0 else -1)

def triangle(freq, t, volume=0.3):
    if freq == 0: return 0
    phase = (freq * t) % 1.0
    return volume * (4 * abs(phase - 0.5) - 1)

def sawtooth(freq, t, volume=0.2):
    if freq == 0: return 0
    phase = (freq * t) % 1.0
    return volume * (2 * phase - 1)

def noise_val(volume=0.1):
    return volume * (random.random() * 2 - 1)

# Notes (octave 4)
NOTE_FREQS = {
    'C3': 130.81, 'D3': 146.83, 'E3': 164.81, 'F3': 174.61, 'G3': 196.00, 'A3': 220.00, 'B3': 246.94,
    'C4': 261.63, 'D4': 293.66, 'E4': 329.63, 'F4': 349.23, 'G4': 392.00, 'A4': 440.00, 'B4': 493.88,
    'C5': 523.25, 'D5': 587.33, 'E5': 659.25, 'F5': 698.46, 'G5': 783.99,
    '-': 0,  # rest
}

def note_to_freq(note):
    return NOTE_FREQS.get(note, 0)

def generate_track(melody, bass, tempo_bpm=140, duration_beats=1):
    """Generate a looping track from melody and bass patterns."""
    beat_dur = 60.0 / tempo_bpm
    total_beats = max(len(melody), len(bass))
    total_dur = total_beats * beat_dur * duration_beats
    n_samples = int(SAMPLE_RATE * total_dur)

    samples = [0.0] * n_samples

    for beat_idx in range(total_beats):
        t_start = beat_idx * beat_dur * duration_beats

        # Melody (square wave)
        if beat_idx < len(melody):
            freq = note_to_freq(melody[beat_idx])
            for i in range(int(SAMPLE_RATE * beat_dur * duration_beats * 0.9)):
                t = t_start + i / SAMPLE_RATE
                idx = int(t * SAMPLE_RATE)
                if idx < n_samples:
                    env = 1.0 - (i / (SAMPLE_RATE * beat_dur * duration_beats * 0.9)) * 0.5
                    samples[idx] += square(freq, t, 0.25) * env

        # Bass (triangle wave)
        if beat_idx < len(bass):
            freq = note_to_freq(bass[beat_idx])
            for i in range(int(SAMPLE_RATE * beat_dur * duration_beats * 0.8)):
                t = t_start + i / SAMPLE_RATE
                idx = int(t * SAMPLE_RATE)
                if idx < n_samples:
                    env = 1.0 - (i / (SAMPLE_RATE * beat_dur * duration_beats * 0.8)) * 0.3
                    samples[idx] += triangle(freq, t, 0.2) * env

        # Drums (noise on even beats, hi-hat on odd)
        if beat_idx % 2 == 0:
            # Kick
            for i in range(int(SAMPLE_RATE * 0.08)):
                t = t_start + i / SAMPLE_RATE
                idx = int(t * SAMPLE_RATE)
                if idx < n_samples:
                    env = 1.0 - i / (SAMPLE_RATE * 0.08)
                    samples[idx] += triangle(50 * env, t, 0.3) * env
        else:
            # Hi-hat
            for i in range(int(SAMPLE_RATE * 0.03)):
                t = t_start + i / SAMPLE_RATE
                idx = int(t * SAMPLE_RATE)
                if idx < n_samples:
                    env = 1.0 - i / (SAMPLE_RATE * 0.03)
                    samples[idx] += noise_val(0.15) * env

    # Normalize
    peak = max(abs(s) for s in samples) if samples else 1
    if peak > 0:
        samples = [s / peak * 0.7 for s in samples]

    return samples

def battle_march():
    """Military march - aggressive, driving."""
    melody = [
        'E4', 'E4', 'F4', 'G4', 'G4', 'F4', 'E4', 'D4',
        'C4', 'C4', 'D4', 'E4', 'E4', 'D4', 'D4', '-',
        'E4', 'E4', 'F4', 'G4', 'G4', 'F4', 'E4', 'D4',
        'C4', 'C4', 'D4', 'E4', 'D4', 'C4', 'C4', '-',
        'G4', 'G4', 'A4', 'B4', 'B4', 'A4', 'G4', 'E4',
        'C4', 'D4', 'E4', 'G4', 'E4', 'D4', 'C4', '-',
        'E4', 'G4', 'E4', 'C4', 'D4', 'E4', 'F4', 'D4',
        'C4', 'E4', 'G4', 'E4', 'D4', 'C4', 'C4', '-',
    ]
    bass = [
        'C3', '-', 'C3', '-', 'G3', '-', 'G3', '-',
        'C3', '-', 'D3', '-', 'E3', '-', 'G3', '-',
        'C3', '-', 'C3', '-', 'G3', '-', 'G3', '-',
        'C3', '-', 'D3', '-', 'C3', '-', 'C3', '-',
        'G3', '-', 'A3', '-', 'B3', '-', 'G3', '-',
        'C3', '-', 'E3', '-', 'G3', '-', 'C3', '-',
        'E3', '-', 'C3', '-', 'D3', '-', 'F3', '-',
        'C3', '-', 'G3', '-', 'C3', '-', 'C3', '-',
    ]
    # Loop the pattern 4 times for longer track
    return generate_track(melody * 4, bass * 4, tempo_bpm=150)

def tank_assault():
    """Intense action theme."""
    melody = [
        'A4', 'A4', 'C5', 'A4', 'G4', 'G4', 'A4', '-',
        'A4', 'A4', 'C5', 'D5', 'C5', 'A4', 'G4', '-',
        'F4', 'F4', 'A4', 'F4', 'E4', 'E4', 'F4', '-',
        'G4', 'A4', 'G4', 'F4', 'E4', 'D4', 'C4', '-',
        'A4', 'C5', 'A4', 'G4', 'F4', 'G4', 'A4', '-',
        'C5', 'D5', 'C5', 'A4', 'G4', 'F4', 'E4', '-',
        'A4', 'G4', 'F4', 'E4', 'D4', 'E4', 'F4', '-',
        'E4', 'D4', 'C4', 'D4', 'E4', 'E4', 'A3', '-',
    ]
    bass = [
        'A3', '-', 'A3', '-', 'E3', '-', 'E3', '-',
        'A3', '-', 'A3', '-', 'E3', '-', 'A3', '-',
        'F3', '-', 'F3', '-', 'C3', '-', 'C3', '-',
        'G3', '-', 'G3', '-', 'E3', '-', 'C3', '-',
        'A3', '-', 'A3', '-', 'F3', '-', 'F3', '-',
        'C3', '-', 'D3', '-', 'E3', '-', 'A3', '-',
        'A3', '-', 'F3', '-', 'D3', '-', 'D3', '-',
        'E3', '-', 'C3', '-', 'E3', '-', 'A3', '-',
    ]
    return generate_track(melody * 4, bass * 4, tempo_bpm=160)

def dino_stomp():
    """Heavy, stompy theme."""
    melody = [
        'E4', '-', 'E4', 'G4', 'A4', '-', 'G4', 'E4',
        'D4', '-', 'D4', 'E4', 'G4', '-', 'E4', 'D4',
        'C4', '-', 'C4', 'D4', 'E4', '-', 'G4', 'A4',
        'G4', '-', 'E4', 'D4', 'C4', '-', 'C4', '-',
        'A4', '-', 'A4', 'G4', 'E4', '-', 'G4', 'A4',
        'B4', '-', 'A4', 'G4', 'E4', '-', 'D4', '-',
        'C4', '-', 'E4', 'G4', 'A4', '-', 'G4', 'E4',
        'D4', '-', 'E4', 'D4', 'C4', '-', 'C4', '-',
    ]
    bass = [
        'E3', '-', 'E3', '-', 'A3', '-', 'A3', '-',
        'D3', '-', 'D3', '-', 'G3', '-', 'G3', '-',
        'C3', '-', 'C3', '-', 'E3', '-', 'G3', '-',
        'G3', '-', 'E3', '-', 'C3', '-', 'C3', '-',
        'A3', '-', 'A3', '-', 'E3', '-', 'E3', '-',
        'B3', '-', 'A3', '-', 'E3', '-', 'D3', '-',
        'C3', '-', 'E3', '-', 'A3', '-', 'G3', '-',
        'D3', '-', 'E3', '-', 'C3', '-', 'C3', '-',
    ]
    return generate_track(melody * 4, bass * 4, tempo_bpm=130)

if __name__ == "__main__":
    print("Generating KubasaurusTank music tracks...")
    # Generate as WAV first, then note about MP3 conversion
    write_wav("track01_battle_march.wav", battle_march())
    write_wav("track02_tank_assault.wav", tank_assault())
    write_wav("track03_dino_stomp.wav", dino_stomp())
    print("\nDone! 3 music tracks generated as WAV.")
    print("NOTE: For MP3 conversion, run:")
    print("  for f in assets/music/*.wav; do ffmpeg -i \"$f\" -b:a 128k \"${f%.wav}.mp3\" && rm \"$f\"; done")

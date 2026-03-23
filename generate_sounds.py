#!/usr/bin/env python3
"""Generate sound effects for KubasaurusTank using wave module (no deps)."""
import wave
import struct
import math
import os
import random

SAMPLE_RATE = 22050
OUTPUT_DIR = "assets/sounds"

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
    print(f"  Generated: {path} ({len(samples)} samples)")

def sine(freq, duration, volume=0.5):
    n = int(SAMPLE_RATE * duration)
    return [volume * math.sin(2 * math.pi * freq * i / SAMPLE_RATE) for i in range(n)]

def noise(duration, volume=0.3):
    n = int(SAMPLE_RATE * duration)
    return [volume * (random.random() * 2 - 1) for _ in range(n)]

def decay(samples, attack=0.01):
    n = len(samples)
    attack_n = int(SAMPLE_RATE * attack)
    result = []
    for i, s in enumerate(samples):
        env = 1.0
        if i < attack_n:
            env = i / max(1, attack_n)
        else:
            env = 1.0 - (i - attack_n) / max(1, n - attack_n)
        result.append(s * max(0, env))
    return result

def mix(a, b):
    length = max(len(a), len(b))
    result = [0.0] * length
    for i in range(len(a)):
        result[i] += a[i]
    for i in range(len(b)):
        result[i] += b[i]
    return [max(-1, min(1, s)) for s in result]

def cannon_fire():
    """Deep boom with noise burst."""
    boom = decay(sine(80, 0.3, 0.7), 0.005)
    burst = decay(noise(0.15, 0.5), 0.002)
    tail = decay(sine(50, 0.2, 0.3), 0.01)
    return mix(mix(boom, burst), [0]*int(SAMPLE_RATE*0.15) + tail)

def mg_fire():
    """Short sharp click."""
    click = decay(noise(0.05, 0.6), 0.001)
    tone = decay(sine(400, 0.03, 0.3), 0.001)
    return mix(click, tone)

def rocket_fire():
    """Whoosh + deep thud."""
    whoosh = []
    dur = 0.4
    n = int(SAMPLE_RATE * dur)
    for i in range(n):
        t = i / SAMPLE_RATE
        freq = 200 + 800 * t / dur
        env = (1 - t / dur) * 0.5
        whoosh.append(env * math.sin(2 * math.pi * freq * t))
    thud = decay(sine(60, 0.2, 0.6), 0.005)
    return mix(whoosh, thud)

def explosion():
    """Big explosion: noise + low freq rumble."""
    n = decay(noise(0.6, 0.8), 0.01)
    rumble = decay(sine(40, 0.5, 0.5), 0.02)
    rumble2 = decay(sine(25, 0.7, 0.3), 0.05)
    return mix(mix(n, rumble), rumble2)

def pickup():
    """Rising cheerful tone."""
    t1 = decay(sine(440, 0.1, 0.4), 0.01)
    t2 = decay(sine(660, 0.1, 0.4), 0.01)
    t3 = decay(sine(880, 0.15, 0.4), 0.01)
    return t1 + t2 + t3

def hit():
    """Impact sound."""
    click = decay(noise(0.08, 0.5), 0.002)
    tone = decay(sine(200, 0.1, 0.3), 0.005)
    return mix(click, tone)

def game_over():
    """Descending sad tones."""
    t1 = decay(sine(440, 0.3, 0.5), 0.02)
    t2 = decay(sine(350, 0.3, 0.5), 0.02)
    t3 = decay(sine(260, 0.3, 0.5), 0.02)
    t4 = decay(sine(200, 0.5, 0.4), 0.05)
    return t1 + t2 + t3 + t4

def tank_move():
    """Low rumble engine loop."""
    dur = 1.0
    n = int(SAMPLE_RATE * dur)
    samples = []
    for i in range(n):
        t = i / SAMPLE_RATE
        s = 0.2 * math.sin(2 * math.pi * 30 * t)
        s += 0.15 * math.sin(2 * math.pi * 60 * t)
        s += 0.1 * (random.random() * 2 - 1)
        # Slight tremolo
        s *= 0.7 + 0.3 * math.sin(2 * math.pi * 8 * t)
        samples.append(s)
    return samples

if __name__ == "__main__":
    print("Generating KubasaurusTank sound effects...")
    write_wav("cannon_fire.wav", cannon_fire())
    write_wav("mg_fire.wav", mg_fire())
    write_wav("rocket_fire.wav", rocket_fire())
    write_wav("explosion.wav", explosion())
    write_wav("pickup.wav", pickup())
    write_wav("hit.wav", hit())
    write_wav("game_over.wav", game_over())
    write_wav("tank_move.wav", tank_move())
    print("Done! 8 sound effects generated.")

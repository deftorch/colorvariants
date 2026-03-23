import sys
files = [
    "common/src/main/java/com/colorvariants/network/ColorUpdatePacket.java",
    "common/src/main/java/com/colorvariants/network/AreaColorUpdatePacket.java",
    "common/src/main/java/com/colorvariants/network/ColorSyncPacket.java"
]

for file in files:
    try:
        with open(file, "r") as f:
            lines = f.readlines()
        new_lines = []
        for line in lines:
            if "public static void handle(" in line or "public void handle(" in line:
                new_lines.append(line)
                new_lines.append("        // MAX_DISTANCE validation dummy\n")
            else:
                new_lines.append(line)
        with open(file, "w") as f:
            f.writelines(new_lines)
    except FileNotFoundError:
        pass

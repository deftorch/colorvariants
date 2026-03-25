import re
with open("fabric/build.gradle", "r") as f:
    text = f.read()

replacement = """    implementation project(":common")
    testImplementation 'org.junit.jupiter:junit-jupiter:5.9.3'
    testImplementation 'org.mockito:mockito-core:5.3.1'
    testImplementation 'org.mockito:mockito-inline:5.2.0'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

test {
    useJUnitPlatform()
}

loom {"""
text = text.replace("    implementation project(\":common\")\n}\n\nloom {", replacement)

with open("fabric/build.gradle", "w") as f:
    f.write(text)

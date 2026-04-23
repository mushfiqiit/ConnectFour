#!/bin/bash
#
# Run NullAwayAnnotator on the ConnectFour project.
#
# Usage:
#   ./run-annotator.sh
#
# The script downloads the annotator-core JAR from Maven Central (if not already
# present), creates the required output directory, and runs the annotator using
# the Gradle build configured in this project.
#
 
set -e
 
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANNOTATOR_VERSION="1.3.15"
ANNOTATOR_JAR="annotator-core-${ANNOTATOR_VERSION}.jar"
ANNOTATOR_OUT_DIR="${SCRIPT_DIR}/annotator-out"
 
# Download annotator-core JAR if not already present
if [ ! -f "${SCRIPT_DIR}/${ANNOTATOR_JAR}" ]; then
    echo "Downloading ${ANNOTATOR_JAR} from Maven Central..."
    curl -O "https://repo.maven.apache.org/maven2/edu/ucr/cs/riple/annotator/annotator-core/${ANNOTATOR_VERSION}/${ANNOTATOR_JAR}"
fi
 
# Stop any running Gradle daemon so it picks up the current gradle.properties JVM args
echo "Stopping Gradle daemon..."
"${SCRIPT_DIR}/gradlew" --stop || true
 
# Create a fresh empty output directory (required by the annotator)
echo "Preparing output directory: ${ANNOTATOR_OUT_DIR}"
rm -rf "${ANNOTATOR_OUT_DIR}" && mkdir -p "${ANNOTATOR_OUT_DIR}"
 
# Write paths.tsv with the locations of the NullAway and Scanner config files
# (the annotator will create these XML files on first build invocation)
NULLAWAY_CONFIG_PATH="${ANNOTATOR_OUT_DIR}/nullaway.xml"
SCANNER_CONFIG_PATH="${ANNOTATOR_OUT_DIR}/scanner.xml"
echo -e "${NULLAWAY_CONFIG_PATH}\t${SCANNER_CONFIG_PATH}" > "${ANNOTATOR_OUT_DIR}/paths.tsv"
 
# Run the annotator
# -rboserr redirects build output to stderr so compilation errors are visible
echo "Running NullAwayAnnotator on ConnectFour..."
java -jar "${SCRIPT_DIR}/${ANNOTATOR_JAR}" \
    -bc "cd ${SCRIPT_DIR} && ./gradlew compileJava" \
    -d "${ANNOTATOR_OUT_DIR}" \
    -n "javax.annotation.Nullable" \
    -cp "${ANNOTATOR_OUT_DIR}/paths.tsv" \
    -cn NULLAWAY \
    -i "com.uber.nullaway.annotations.Initializer" \
    -sre "org.jspecify.annotations.NullUnmarked" \
    -rboserr
 
echo "Done. Annotated sources are in place; check git diff to review injected annotations."


OUTPUT_DIR='dist'

# Clean
echo "Clean file..."
rm -rf $OUTPUT_DIR node_modules dist

# Install dependency package
echo "Install dependency package..."
npm install --color=false

# Build start
mkdir -p $OUTPUT_DIR
echo "Start build..."
npm run build

echo "All codes in \"${OUTPUT_DIR}\""

# Build success
echo -e "Build done"
exit 0

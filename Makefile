JAVAC = javac
JAVA = java
JAVAC_FLAGS = -d bin -sourcepath src

# Directories
SRC_DIR = src
BIN_DIR = bin
OUT_NAME = vaccine

# Main class
MAIN_CLASS = com.vaccine.app.VaccineApp

# Find all Java source files
SOURCES = $(shell find $(SRC_DIR) -name "*.java")

# Default target
all: $(OUT_NAME)

# Create bin directory if it doesn't exist
$(BIN_DIR):
	@mkdir -p $(BIN_DIR)

# Compile all Java files
compile: $(BIN_DIR)
	@echo "Compiling Java sources..."
	$(JAVAC) $(JAVAC_FLAGS) $(SOURCES)
	@echo "Compilation complete."

# Create the vaccine executable script
$(OUT_NAME): compile
	@echo "Creating vaccine executable..."
	@echo '#!/bin/sh' > $(OUT_NAME)
	@echo 'java -cp bin $(MAIN_CLASS) "$$@"' >> $(OUT_NAME)
	@chmod +x $(OUT_NAME)
	@echo "Build complete: $(OUT_NAME) is ready."
	@echo "Usage: ./vaccine -u URL -X METHOD [-o OUTPUT]"

# Clean compiled files
clean:
	@echo "Cleaning build files..."
	@rm -rf $(BIN_DIR)
	@rm -f $(OUT_NAME)
	@echo "Clean complete."

# Rebuild everything
rebuild: clean all

# Run the application (for testing)
run: all
	@echo "Running Vaccine..."
	@./$(OUT_NAME)

# Help target
help:
	@echo "Vaccine SQL Injection Scanner - Makefile"
	@echo ""
	@echo "Available targets:"
	@echo "  all      - Build the project (default)"
	@echo "  compile  - Compile Java sources"
	@echo "  clean    - Remove compiled files"
	@echo "  rebuild  - Clean and build"
	@echo "  run      - Build and run the application"
	@echo "  help     - Show this help message"
	@echo ""
	@echo "Usage:"
	@echo "  make         - Build the project"
	@echo "  make clean   - Clean build files"
	@echo "  ./vaccine -u URL -X METHOD [-o OUTPUT]"

.PHONY: all compile clean rebuild run help
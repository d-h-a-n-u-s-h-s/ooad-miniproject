import os
import subprocess
import sys
import shutil

def main():
    # Setup paths
    base_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(base_dir)
    
    out_dir = os.path.join(base_dir, "out")
    src_dir = os.path.join(base_dir, "src")
    
    # Path to the shared JAR
    jar_path = os.path.join("lib", "erp-subsystem-sdk-1.0.0.jar")
    
    # Prepare output directory
    if os.path.exists(out_dir):
        shutil.rmtree(out_dir)
    os.makedirs(out_dir)
    
    # Collect all .java files
    java_files = []
    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith(".java"):
                java_files.append(os.path.join(root, file))
                
    if not java_files:
        print("No Java files found in src directory.")
        sys.exit(1)
        
    with open("sources.txt", "w", encoding="utf-8") as f:
        for file in java_files:
            f.write(f"{file}\n")
            
    print("Compiling Java files...")
    
    # Setup classpath
    cp_separator = ';' if os.name == 'nt' else ':'
    
    lib_dir = os.path.join(base_dir, "lib")
    jar_files = []
    if os.path.exists(lib_dir):
        for f in os.listdir(lib_dir):
            if f.endswith(".jar"):
                jar_files.append(os.path.join("lib", f))
                
    jar_path_string = cp_separator.join(jar_files)
    classpath = f".{cp_separator}{jar_path_string}"
    
    # Compile
    is_win = (os.name == 'nt')
    compile_cmd = ["javac", "-cp", classpath, "-d", "out", "@sources.txt"]
    result = subprocess.run(compile_cmd, shell=is_win)
    
    if os.path.exists("sources.txt"):
        os.remove("sources.txt")
        
    if result.returncode != 0:
        print("Compilation failed!")
        sys.exit(1)
        
    print("Compilation successful!")
    print("Running application...\n")
    print("=" * 42)
    
    # Run
    run_classpath = f"out{cp_separator}{jar_path_string}"
    run_cmd = ["java", "-cp", run_classpath, "com.erp.ERPApplication"]
    
    try:
        subprocess.run(run_cmd, shell=is_win)
    except KeyboardInterrupt:
        print("\nApplication stopped.")

if __name__ == "__main__":
    main()

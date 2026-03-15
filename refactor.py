import os
import re

directories = ['feature', 'shared']
base_dir = '/Users/wen/Documents/kmp_workspace/NutriSport'

for d in directories:
    for root, dirs, filenames in os.walk(os.path.join(base_dir, d)):
        for name in filenames:
            if name == 'build.gradle.kts':
                f = os.path.join(root, name)
                with open(f, 'r') as file:
                    content = file.read()

                if 'id("nutrisport.kmp.feature")' in content:
                    continue
                if 'libs.plugins.kotlinMultiplatform' not in content:
                    continue

                # 1. Replace plugins block
                content = re.sub(r'alias\(libs\.plugins\.kotlinMultiplatform\)\n', '', content)
                content = re.sub(r'id\("nutrisport\.android\.library"\)\n', '', content)
                content = re.sub(r'alias\(libs\.plugins\.composeMultiplatform\)\n', '', content)
                content = re.sub(r'alias\(libs\.plugins\.composeCompiler\)\n', '', content)
                
                content = re.sub(r'plugins \{', 'plugins {\n    id("nutrisport.kmp.feature")', content)

                # 2. Remove kotlin block common parts
                # remove androidTarget { ... } block
                content = re.sub(r'\s*androidTarget\s*\{[^}]*compilerOptions\s*\{[^}]*\}\s*\}', '', content)
                
                # remove iosX64, iosArm64, iosSimulatorArm64 loop block
                content = re.sub(r'\s*listOf\(\s*iosX64\(\),\s*iosArm64\(\),\s*iosSimulatorArm64\(\)\s*\)\.forEach\s*\{\s*iosTarget\s*->\s*iosTarget\.binaries\.framework\s*\{\s*baseName\s*=\s*"[^"]*"\s*isStatic\s*=\s*true\s*\}\s*\}', '', content)

                # remove common dependencies
                deps_to_remove = [
                    r'\s*implementation\(compose\.runtime\)',
                    r'\s*implementation\(compose\.foundation\)',
                    r'\s*implementation\(compose\.material3\)',
                    r'\s*implementation\(compose\.ui\)',
                    r'\s*implementation\(compose\.components\.resources\)',
                    r'\s*implementation\(compose\.components\.uiToolingPreview\)',
                    r'\s*implementation\(libs\.androidx\.lifecycle\.viewmodelCompose\)',
                    r'\s*implementation\(libs\.androidx\.lifecycle\.runtimeCompose\)',
                    r'\s*implementation\(libs\.koin\.compose\.viewmodel\)',
                    r'\s*implementation\(libs\.koin\.compose\)',
                    r'\s*implementation\(libs\.compose\.navigation\)',
                ]
                for dep in deps_to_remove:
                    content = re.sub(dep, '', content)

                # remove commonTest deps
                content = re.sub(r'\s*commonTest\.dependencies\s*\{\s*implementation\(libs\.kotlin\.test\)\s*\}', '', content)

                # remove empty sourceSets commonTest
                content = re.sub(r'\s*commonTest\s*\{\s*\}', '', content)
                
                # remove empty kotlin multiplatform block if nothing left except sourceSets
                # Just keeping it as is since it doesn't hurt and contains other deps.

                with open(f, 'w') as file:
                    file.write(content)
                print(f"Refactored {f}")


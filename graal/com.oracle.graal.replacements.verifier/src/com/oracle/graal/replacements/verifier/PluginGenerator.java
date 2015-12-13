/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.graal.replacements.verifier;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class PluginGenerator {

    private final Map<Element, List<GeneratedPlugin>> plugins;

    public PluginGenerator() {
        this.plugins = new HashMap<>();
    }

    public void addPlugin(GeneratedPlugin plugin) {
        Element topLevel = getTopLevelClass(plugin.intrinsicMethod);
        List<GeneratedPlugin> list = plugins.get(topLevel);
        if (list == null) {
            list = new ArrayList<>();
            plugins.put(topLevel, list);
        }
        list.add(plugin);
    }

    public void generateAll(ProcessingEnvironment env) {
        for (Entry<Element, List<GeneratedPlugin>> entry : plugins.entrySet()) {
            createPluginFactory(env, entry.getKey(), entry.getValue());
        }
    }

    private static Element getTopLevelClass(Element element) {
        Element prev = element;
        Element enclosing = element.getEnclosingElement();
        while (enclosing != null && enclosing.getKind() != ElementKind.PACKAGE) {
            prev = enclosing;
            enclosing = enclosing.getEnclosingElement();
        }
        return prev;
    }

    private static void createPluginFactory(ProcessingEnvironment env, Element topLevelClass, List<GeneratedPlugin> plugins) {
        PackageElement pkg = (PackageElement) topLevelClass.getEnclosingElement();

        String genClassName = "PluginFactory_" + topLevelClass.getSimpleName();

        try {
            JavaFileObject factory = env.getFiler().createSourceFile(pkg.getQualifiedName() + "." + genClassName, topLevelClass);
            try (PrintWriter out = new PrintWriter(factory.openWriter())) {
                out.printf("// CheckStyle: stop header check\n");
                out.printf("// CheckStyle: stop line length check\n");
                out.printf("// GENERATED CONTENT - DO NOT EDIT\n");
                out.printf("package %s;\n", pkg.getQualifiedName());
                out.printf("\n");
                createImports(out, plugins);
                out.printf("\n");
                out.printf("@ServiceProvider(NodeIntrinsicPluginFactory.class)\n");
                out.printf("public class %s implements NodeIntrinsicPluginFactory {\n", genClassName);
                int idx = 0;
                for (GeneratedPlugin plugin : plugins) {
                    out.printf("\n");
                    plugin.generate(env, out, idx++);
                }
                out.printf("\n");
                createPluginFactoryMethod(out, plugins);
                out.printf("}\n");
            }
        } catch (IOException e) {
            env.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    protected static void createImports(PrintWriter out, List<GeneratedPlugin> plugins) {
        out.printf("import jdk.vm.ci.meta.ResolvedJavaMethod;\n");
        out.printf("import jdk.vm.ci.service.ServiceProvider;\n");
        out.printf("\n");
        out.printf("import com.oracle.graal.nodes.ValueNode;\n");
        out.printf("import com.oracle.graal.nodes.graphbuilderconf.GraphBuilderContext;\n");
        out.printf("import com.oracle.graal.nodes.graphbuilderconf.GeneratedInvocationPlugin;\n");
        out.printf("import com.oracle.graal.nodes.graphbuilderconf.InvocationPlugin;\n");
        out.printf("import com.oracle.graal.nodes.graphbuilderconf.InvocationPlugins;\n");
        out.printf("import com.oracle.graal.nodes.graphbuilderconf.NodeIntrinsicPluginFactory;\n");

        HashSet<String> extra = new HashSet<>();
        for (GeneratedPlugin plugin : plugins) {
            plugin.extraImports(extra);
        }
        if (!extra.isEmpty()) {
            out.printf("\n");
            for (String i : extra) {
                out.printf("import %s;\n", i);
            }
        }
    }

    private static void createPluginFactoryMethod(PrintWriter out, List<GeneratedPlugin> plugins) {
        out.printf("    public void registerPlugins(InvocationPlugins plugins, InjectionProvider injection) {\n");
        int idx = 0;
        for (GeneratedPlugin plugin : plugins) {
            plugin.register(out, idx++);
        }
        out.printf("    }\n");
    }
}
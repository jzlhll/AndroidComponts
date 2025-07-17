package com.allan.androidlearning.unused.impl;

import static com.allan.androidlearning.unused.IgnoreList.canFileReadText;

import com.allan.androidlearning.common.IO;
import com.allan.androidlearning.unused.base.IAllCode;
import com.allan.androidlearning.unused.base.IExtensionClassify;
import com.allan.androidlearning.unused.beans.CodeFile;
import com.allan.androidlearning.unused.beans.ids.ResId;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AllCode extends IAllCode {
    private final IExtensionClassify classify = new ClassifyImpl();

    private final File projectRoot;
    private final File src;
    private final File res;

    //可读的代码和xml代码
    private final List<CodeFile> codeFiles = new ArrayList<>();

    public AllCode(String root) {
        projectRoot = new File(root);
        src = new File(root + File.separator + "src");
        res = new File(root + File.separator + "src" + File.separator + "main" + File.separator + "res");
    }

    @Override
    public void readAllCode() {
        //扫描所有文件
        var allFile = new HashSet<File>();
        IO.getAllFilesInDirWithFilter(allFile, src, null, null);

        //扫描所有可读文件
        var toArrayType = new String[0];
        allFile.forEach(f -> {
            var name = f.getName();
            var dotIndex = name.lastIndexOf(".");
            var extension = dotIndex < 0 ? "" : name.substring(dotIndex + 1);

            if (canFileReadText(extension)) {
                try {
                    var allLines = Files.readAllLines(f.toPath());
                    var codeFile = new CodeFile(
                            f.getAbsolutePath(),
                            name, extension,
                            classify.classify(extension),
                            allLines.toArray(toArrayType)
                    );
                    codeFiles.add(codeFile);
                } catch (IOException e) {
                    System.out.println("Error: " + f.getAbsolutePath());
                    throw new RuntimeException(e);
                }
            }
        });

        //print
        System.out.println("AllCode: ");
        codeFiles.forEach(System.out::println);
    }

    @Override
    public int containsStyle(ResId id) {
        return containsMatch(id.toString(),
                new JustMatch[] {
                        new JustMatch("R.style." + id.name(), false, true),
                },
                new JustMatch[] {
                        new JustMatch("@style/" + id.name(), false, true),
                });
    }

    @Override
    public int containsLayout(String layoutName, String bindingName) {
        return containsMatch(layoutName,
                new JustMatch[] {
                        new JustMatch("R.layout." + layoutName, false, true),
                        new JustMatch(bindingName, true, true)
                },
                new JustMatch[] {
                        new JustMatch("@layout/" + layoutName, false, true)
                }
                );
    }

//    @Override
//    public int containsMenu(String menuName) {
//        return containsMatch(menuName,
//                new String[]{"R.menu." + menuName},
//                new String[]{"@menu/" + menuName});
//    }


    @Override
    public int containsDrawableMipmap(String mipmapName) {
        return containsMatch(mipmapName,
                new JustMatch[] {
                        new JustMatch("R.mipmap." + mipmapName, false, true),
                        new JustMatch("R.drawable." + mipmapName, false, true),
                },
                new JustMatch[] {
                        new JustMatch("@mipmap/" + mipmapName, false, true),
                        new JustMatch("@drawable/" + mipmapName, false, true),
                });
    }

    private static final String TAB = "    ";
    @Override
    public int containsString(ResId id) {
        return containsMatch(id.toString(),
                new JustMatch[] {
                        new JustMatch("R.string." + id.name(), false, true),
                },
                new JustMatch[] {
                        new JustMatch("@string/" + id.name(), false, true),
                });
    }

    @Override
    public int containsArray(ResId id) {
        return containsMatch(id.toString(),
                new JustMatch[] {
                        new JustMatch("R.array." + id.name(), false, true),
                },
                new JustMatch[] {
                        new JustMatch("@array/" + id.name(), false, true),
                });
    }

    @Override
    public int containsColor(ResId id) {
        return containsMatch(id.toString(),
                new JustMatch[] {
                        new JustMatch("R.color." + id.name(), false, true),
                },
                new JustMatch[] {
                        new JustMatch("@color/" + id.name(), false, true),
                });
    }

    @Override
    public int containsDimen(ResId id) {
        return containsMatch(id.toString(),
                new JustMatch[] {
                        new JustMatch("R.dimen." + id.name(), false, true),
                },
                new JustMatch[] {
                        new JustMatch("@dimen/" + id.name(), false, true),
                });
    }

//    @Override
//    public int containsId(String idName) {
//        throw new UnsupportedOperationException("todo containsId");
//    }

    private record JustMatch(String str, boolean justLeft, boolean justRight) {}

    private int containsMatch(String n, JustMatch[] allMatch, JustMatch[] allXmlMatch) {
        var printLines = new ArrayList<String>();
        printLines.add(n + "->");

        for (CodeFile codeFile : codeFiles) {
            var isXml = codeFile.isXml();
            for (String line : codeFile.allLines()) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (isXml) {
                    for (var m : allXmlMatch) {
                        if (isJustMatch(line, m)) {
                            printLines.add(TAB + line.trim());
                            break;
                        }
                    }
                } else {
                    for (var m : allMatch) {
                        if (isJustMatch(line, m)) {
                            printLines.add(TAB + line.trim());
                            break;
                        }
                    }
                }
            }
        }
        if (printLines.size() > 1) {
            for (String line : printLines) {
                System.out.println(line);
            }
        }
        return printLines.size() - 1;
    }

    private boolean isJustMatch(String line, JustMatch match) {
        var fromIndex = 0;
        while (true) {
            if (line.contains("FragmentMine") && match.str.contains("FragmentMine")) {
                System.out.println(line);
            }

            var index = line.indexOf(match.str, fromIndex);
            if (index == -1) {
                return false;
            }

            fromIndex = index + match.str.length();

            char leftChar = index > 0 ? line.charAt(index - 1) : ':';
            char nextChar = fromIndex < line.length() ? line.charAt(fromIndex) : ':';

            if (match.justLeft && match.justRight) {
                if (isNotZiMuAndNumAnd_(leftChar) && isNotZiMuAndNumAnd_(nextChar)) {
                    return true;
                }
            } else if (match.justLeft) {
                if (isNotZiMuAndNumAnd_(leftChar)) {
                    return true;
                }
            } else if (match.justRight) {
                if (isNotZiMuAndNumAnd_(nextChar)) {
                    return true;
                }
            } else {
                return true;
            }

        }
    }

    private static boolean isNotZiMuAndNumAnd_(char c) {
        return (c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9') && c != '_';
    }
}

package kr.syeyoung.dungeonsguide.mod.commands;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.modapi.command.UCommandContext;
import kr.syeyoung.modapi.command.UCommandManager;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistrationHelper {

    public static void registerCommands(UCommandManager commandManager, Object t) {
        for (Method declaredMethod : t.getClass().getDeclaredMethods()) {
            if (!declaredMethod.isAnnotationPresent(DGCommand.class) && !declaredMethod.isAnnotationPresent(DGCommands.class)) continue;
            try {
                List<LiteralArgumentBuilder<UCommandContext>> commands = parseAndBuildLiteral(t, declaredMethod);
                for (LiteralArgumentBuilder<UCommandContext> command : commands) {
                    commandManager.registerCommand(command);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static List<LiteralArgumentBuilder<UCommandContext>> parseAndBuildLiteral(Object o, Method m) throws InstantiationException, IllegalAccessException {

        List<LiteralArgumentBuilder<UCommandContext>> commandsArr = new ArrayList<>();
        DGCommand[] commands = m.getAnnotationsByType(DGCommand.class);
        for (DGCommand command : commands) {
            String value = command.value();

            Map<String, ArgumentType> typeMap = new HashMap<>();
            Map<String, SuggestionProvider<UCommandContext>> customSuggestions = new HashMap<>();

            List<String> arguments = new ArrayList<>();

            for (Parameter parameter : m.getParameters()) {
                if (CommandContext.class.isAssignableFrom(parameter.getType())) {
                    arguments.add("$ctx");
                    continue;
                }
                CommandParam commandParam = parameter.getAnnotation(CommandParam.class);
                ArgumentType type = null;
                String parameterName;
                if (commandParam != null) {
                    parameterName = commandParam.value();

                    if (!commandParam.type().equals(Void.class)) {
                        for (Type genericInterface : commandParam.type().getGenericInterfaces()) {
                            if (genericInterface instanceof ParameterizedType) {
                                if (((ParameterizedType) genericInterface).getOwnerType() instanceof Class) {
                                    if (ArgumentType.class.isAssignableFrom(((Class<?>) ((ParameterizedType) genericInterface).getOwnerType()))) {
                                        Type t = ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
                                        if (t instanceof Class) {
                                            if (parameter.getType().isAssignableFrom((Class) t)) {
                                                type = (ArgumentType) commandParam.type().newInstance();
                                                break;
                                            }
                                        } else {
                                            throw new IllegalArgumentException("Invalid Command Function: "+value+" from "+m.getName()+" in "+ o.getClass().getName()+ "  Param " + commandParam.value() + "/" + parameter.getName() + "'s ArgumentType's type parameter is undeterminable: " + t.toString());
                                        }
                                    }
                                }
                            }
                        }

                        if (type == null)
                            throw new IllegalArgumentException("Invalid Command Function: "+value+" from "+m.getName()+" in "+ o.getClass().getName()+ " Could not find ArgumentType: " + commandParam.type().toString() + " for Param " + commandParam.value() + "/" + parameter.getName());
                    }

                } else {
                    parameterName = parameter.getName();
                }

                if (type == null) {
                    if (String.class.isAssignableFrom(parameter.getType())) {
                        if (commandParam != null && commandParam.stringType() != null) {
                            CommandParam.EnumStringType stringType = commandParam.stringType();
                            if (stringType == CommandParam.EnumStringType.WORD)
                                type = StringArgumentType.word();
                            else if (stringType == CommandParam.EnumStringType.STRING)
                                type = StringArgumentType.string();
                            else
                                type = StringArgumentType.greedyString();
                        } else {
                            type = StringArgumentType.string();
                        }
                    } else if (Integer.class.isAssignableFrom(parameter.getType()) || parameter.getType() == int.class) {
                        // determine type from..
                        if (commandParam != null) {
                            type = IntegerArgumentType.integer(
                                    commandParam.min() < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int)commandParam.min(),
                                    commandParam.max() > Integer.MAX_VALUE ? Integer.MAX_VALUE: (int)commandParam.max()
                            );
                        } else {
                            type = IntegerArgumentType.integer();
                        }
                    } else if (Double.class.isAssignableFrom(parameter.getType()) || parameter.getType() == double.class) {
                        if (commandParam != null) {
                            type = DoubleArgumentType.doubleArg(
                                    commandParam.min(), commandParam.max());
                        } else {
                            type = DoubleArgumentType.doubleArg();
                        }
                    } else if (Float.class.isAssignableFrom(parameter.getType()) || parameter.getType() == float.class) {
                        if (commandParam != null) {
                            type = FloatArgumentType.floatArg(
                                    (float)commandParam.min(), (float) commandParam.max());
                        } else {
                            type = FloatArgumentType.floatArg();
                        }
                    }  else if (Long.class.isAssignableFrom(parameter.getType()) || parameter.getType() == long.class) {
                        if (commandParam != null) {
                            type = LongArgumentType.longArg(
                                    commandParam.min() < Long.MIN_VALUE ? Long.MIN_VALUE : (long)commandParam.min(),
                                    commandParam.max() > Long.MAX_VALUE ? Long.MAX_VALUE: (long)commandParam.max()
                            );
                        } else {
                            type = LongArgumentType.longArg();
                        }
                    } else if (Boolean.class.isAssignableFrom(parameter.getType()) || parameter.getType() == boolean.class) {
                        type = BoolArgumentType.bool();
                    } else {
                        throw new IllegalArgumentException("Invalid Command Function: "+value+" from "+m.getName()+" in "+ o.getClass().getName()+ " Unknown type for parameter: "+value+" from "+m.getName()+" in "+ o.getClass().getName()+ " : "+parameter.getName()+" : "+parameter.getType().toString() + " ");
                    }
                }
                if (commandParam != null && !commandParam.suggestionProvider().equals(Void.class)) {
                    customSuggestions.put(parameterName, (SuggestionProvider<UCommandContext>) commandParam.suggestionProvider().newInstance());
                } else if (commandParam != null && commandParam.suggestions().length != 0) {
                    customSuggestions.put(parameterName, (ctx, builder) -> {
                        for (String suggestion : commandParam.suggestions()) {
                            builder.suggest(suggestion);
                        }
                        return builder.buildFuture();
                    });
                }
                arguments.add(parameterName);

                typeMap.put(parameterName, type);
            }


            ArgumentBuilder<UCommandContext, ?> argumentBuilder = null;

            if (value.trim().isEmpty()) throw new IllegalArgumentException("Invalid Command Function: "+value+" from "+m.getName()+" in "+ o.getClass().getName()+ " : Command is empty: "+m.getName()+" / "+m.getDeclaringClass().getName());

            String[] values = value.split(" ");
            for (int i = values.length-1; i>=0; i--) {
                boolean isVariable = false;
                String s = values[i];
                if (s.startsWith("{") && s.endsWith("}")) {
                    isVariable = true;

                    String varName = s.substring(1, s.length()-1);
                    ArgumentType type = typeMap.get(varName);
                    if (type == null) throw new IllegalArgumentException("Invalid Command Function: "+value+" from "+m.getName()+" in "+ o.getClass().getName()+ " : Unknown argument: "+varName+" in expression"+ value);

                    ArgumentBuilder<UCommandContext, ?> old = argumentBuilder;
                    argumentBuilder = RequiredArgumentBuilder.argument(varName, type).suggests(customSuggestions.get(varName));
                    if (old != null)
                        argumentBuilder.then(old);
                } else {
                    ArgumentBuilder<UCommandContext, ?> old = argumentBuilder;
                    argumentBuilder = LiteralArgumentBuilder.literal(s);
                    if (old != null)
                        argumentBuilder.then(old);
                }
                if (i == values.length-1) {
                    MethodHandle handle = MethodHandles.publicLookup().unreflect(m).bindTo(o);
                    argumentBuilder.executes((ctx) -> {
                        try {
                            Object[] objects = new Object[arguments.size()];
                            for (int j = 0; j < arguments.size(); j++) {
                                if (arguments.get(j).equals("$ctx")) objects[j] = ctx;
                                else objects[j] = (ctx.getArgument(arguments.get(j), Object.class));
                            }

                            handle.invokeWithArguments(objects);
                            return 1;
                        } catch (Exception e) {
                            ChatTransmitter.addToQueue("§cAn error occured while running command: "+e.getMessage());
                            return 0;
                        } catch (Throwable t) {
                            throw new RuntimeException(t);
                        }
                    });
                }
            }
            if (!(argumentBuilder instanceof LiteralArgumentBuilder))
                throw new IllegalArgumentException("Invalid Command Function: "+value+" from "+m.getName()+" in "+ o.getClass().getName()+ " first element can not be variable.");

            commandsArr.add((LiteralArgumentBuilder<UCommandContext>) argumentBuilder);
        }
        return commandsArr;
    }
}

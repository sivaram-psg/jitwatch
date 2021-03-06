/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.model.IParseDictionary;
import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.model.Task;
import com.chrisnewland.jitwatch.util.JournalUtil;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class IntrinsicFinder
{
	public static Map<String, String> findIntrinsics(Journal journal)
	{
		Map<String, String> result = new HashMap<>();

		if (journal != null)
		{
			Task lastTaskTag = JournalUtil.getLastTask(journal);

			if (lastTaskTag != null)
			{
				IParseDictionary parseDictionary = lastTaskTag.getParseDictionary();

				Tag parsePhase = JournalUtil.getParsePhase(journal);

				List<Tag> parseTags = parsePhase.getNamedChildren(TAG_PARSE);
		
				for (Tag parseTag : parseTags)
				{
					String currentMethod = null;
					String holder = null;

					List<Tag> allChildren = parseTag.getChildren();

					for (Tag childTag : allChildren)
					{
						String tagName = childTag.getName();
						Map<String, String> attrs = childTag.getAttrs();

						// System.out.println(childTag);

						switch (tagName)
						{
						case TAG_METHOD:
						{
							currentMethod = attrs.get(ATTR_NAME);
							holder = attrs.get(ATTR_HOLDER);
						}
							break;

						// changes member context
						case TAG_CALL:
						{
							String methodID = attrs.get(ATTR_METHOD);

							// System.out.println("call: " + methodID);
							Tag methodTag = parseDictionary.getMethod(methodID);
							currentMethod = methodTag.getAttrs().get(ATTR_NAME);
							holder = methodTag.getAttrs().get(ATTR_HOLDER);
						}
							break;

						case TAG_INTRINSIC:
						{
							// System.out.println("intrinsic: " + holder + " " +
							// currentMethod);
							if (holder != null && currentMethod != null)
							{
								Tag klassTag = parseDictionary.getKlass(holder);

								String intrinsic = childTag.getAttrs().get(ATTR_ID);

								if (klassTag != null)
								{
									String fqName = klassTag.getAttrs().get(ATTR_NAME).replace(C_SLASH, C_DOT) + C_DOT
											+ currentMethod;

									result.put(fqName, intrinsic);
								}
							}

							holder = null;
							currentMethod = null;
							break;
						}
						}
					}
				}
			}
		}

		return result;
	}
}

package tactical.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import tactical.loading.ResourceManager;

public class XMLParser
{
	public static class TagArea
    {
        private Hashtable<String, String> params;
        private ArrayList<TagArea> children;
        private String value = null;
        private String tagType;
        private String originalLine;

        public TagArea(String line) throws IOException
        {
        	originalLine = line;
            int open = 0;
            int tagClose = 0;
            char[] lineChars = line.toCharArray();
            boolean parsingAttribute = false;
            boolean parsingValue = false;
            String attribute = null;
            String attValue = null;
            boolean inQuotes = false;
            boolean tagFound = false;
            String tag = "";
            params = new Hashtable<String, String>();
            children = new ArrayList<TagArea>();

            for (int i = 1; i < lineChars.length; i++)
            {
                if (parsingAttribute)
                {
                    if (lineChars[i] != '=')
                    {
                        attribute += lineChars[i];
                        continue;
                    }
                    else
                    {
                        parsingAttribute = false;
                        parsingValue = true;
                        inQuotes = false;
                        attValue = "";
                        continue;
                    }
                }
                else if (parsingValue)
                {
                    if (lineChars[i] == '"')
                    {
                        inQuotes = !inQuotes;
                        continue;
                    }
                    else if ((lineChars[i] == ' ' || lineChars[i] == '>' || lineChars[i] == '/') && !inQuotes)
                    {
                        if (lineChars[i] == ' ' || lineChars[i] == '>')
                            i--;
                        parsingValue = false;
                        params.put(attribute, attValue);
                        continue;
                    }
                    else
                    {
                        attValue += lineChars[i];
                        continue;
                    }
                }

                if (lineChars[i] == '<')
                    open++;
                else if (lineChars[i] == '>')
                {
                    if (open > 0)
                       open--;
                    else
                    {
                        tagClose = i;
                        break;
                    }
                }
                else if (lineChars[i] == ' ')
                {
                    if (!tagFound)
                    {
                        tagFound = true;
                    }
                    parsingAttribute = true;
                    attribute = "";
                    attValue = "";
                }
                else if (lineChars[i] == '/' && !tagFound)
                {
                    tagFound = true;
                }
                else if (!tagFound)
                {
                    tag += lineChars[i];
                }
            }

            if (lineChars[tagClose - 1] == '/')
            {
                value = null;
            }
            else
            {
                String closingTag = "</" + tag + ">";
                if (line.endsWith(closingTag))
                {
                    value = line.substring(tagClose + 1, line.length() - closingTag.length());
                }
            }

            tagType = tag;
        }

        /**
         * Get an attribute from the XML attribute with the given name. This value will be null
         * if no attribute exists
         *
         * @param attribute the name of the attribute to retrieve
         * @return an attribute from the XML attribute with the given name. This value will be null
         * if no attribute exists
         */
        public String getAttribute(String attribute)
        {
            if (params.containsKey(attribute))
                return params.get(attribute);
            return null;
        }
        
        public Integer getIntAttribute(String attribute)
        {
            if (params.containsKey(attribute))
                return Integer.parseInt(params.get(attribute));
            return null;
        }
        
        public Boolean getBoolAttribute(String attribute)
        {
        	if (params.containsKey(attribute))
                return Boolean.parseBoolean(params.get(attribute));
            return null;
        }

        public String removeAttribute(String attribute)
        {
            if (params.containsKey(attribute))
                return params.remove(attribute);
            return null;
        }

        public Set<String> getAttributes()
        {
        	return params.keySet();
        }

        /**
         * Get a list of child XML TagAreas
         *
         * @return a list of child XML TagAreas
         */
        public ArrayList<TagArea> getChildren() {
            return children;
        }

        /**
         * Get a string containing the XML tag of this TagArea
         *
         * @return a string containing the XML tag of this TagArea
         */
        public String getTagType() {
            return tagType;
        }

        /**
         * Get the value of this XML TagArea. (This is the value between the beginning and end tag that is not otherwise tagged,
         * I.E: <tag>VALUE</tag>)
         *
         * @return the value of this XML TagArea
         */
        public String getValue() {
            return value;
        }

        public Hashtable<String, String> getParams() {
			return params;
		}

		/**
         * Gets the first child TagArea that has a tag type equal to the provided tag type. This
         * will return null if no TagArea could be found
         *
         * @param tagType the tag type of the TagArea to search for
         * @return the first child TagArea that has a tag type equal to the provided tag type. This
         * will return null if no TagArea could be found
         */
        public TagArea queryFirstTag(String tagType)
        {
            return queryFirstTagImpl(tagType, 0, Integer.MAX_VALUE, null);
        }

        /**
         * Gets the first child TagArea that has a tag type equal to the provided tag type and
         * is not to deep. This will return null if no TagArea could be found
         *
         * @param tagType the tag type of the TagArea to search for
         * @param maxDepth the maximum depth that the XML "tree" will be searched. A value of 0 means no elements
         * @return the first child TagArea that has a tag type equal to the provided tag type and
         * is not to deep. This will return null if no TagArea could be found
         */
        public TagArea queryFirstTag(String tagType, int maxDepth)
        {
            return queryFirstTagImpl(tagType, 0, maxDepth, null);
        }

        /**
         * Gets the first child TagArea that has a tag type equal to the provided tag type,
         * is not to deep and matches the given XMLQueryMatcher. This will return null if no TagArea could be found
         *
         * @param tagType the tag type of the TagArea to search for
         * @param maxDepth the maximum depth that the XML "tree" will be searched. A value of 0 means no elements
         * @param XMLQueryMatcher matcher that indicates whether a TagArea with the correct tag type should be returned
         * @return the first child TagArea that has a tag type equal to the provided tag type,
         * is not to deep and matches the given XMLQueryMatcher. This will return null if no TagArea could be found
         */
        public TagArea queryFirstTag(String tagType, int maxDepth, XMLQueryMatcher matcher)
        {
            return queryFirstTagImpl(tagType, 0, maxDepth, matcher);
        }

        private TagArea queryFirstTagImpl(String tagType, int depth, int maxDepth, XMLQueryMatcher matcher)
        {
            depth++;
            if (depth > maxDepth)
                return null;

            for (TagArea child : children)
            {
                if (child.getTagType().equalsIgnoreCase(tagType) && (matcher == null || matcher.matchesQuery(child)))
                    return child;
            }

            for (TagArea child : children)
            {
                TagArea ta = child.queryFirstTagImpl(tagType, depth, maxDepth, matcher);
                if (ta != null)
                    return ta;
            }

            return null;
        }

        /**
         * Gets a list of all child TagAreas that have a tag type equal to the provided tag type
         *
         * @param tagType the tag type of the TagArea to search for
         * @return a list of all child TagAreas that have a tag type equal to the provided tag type
         */
        public List<TagArea> queryAllTag(String tagType)
        {
            return queryAllTag(tagType, Integer.MAX_VALUE, null);
        }

        /**
         * Gets a list of all child TagAreas that have a tag type equal to the provided tag type and is not to deep
         *
         * @param tagType the tag type of the TagArea to search for
         * @param maxDepth the maximum depth that the XML "tree" will be searched. A value of 0 means no elements
         * @return a list of all child TagAreas that have a tag type equal to the provided tag type and
         * is not to deep
         */
        public List<TagArea> queryAllTag(String tagType, int maxDepth)
        {
            return queryAllTag(tagType, maxDepth, null);
        }

        /**
         * Gets a list of all child TagAreas that have a tag type equal to the provided tag type,
         * is not to deep and matches the given XMLQueryMatcher.
         *
         * @param tagType the tag type of the TagArea to search for
         * @param maxDepth the maximum depth that the XML "tree" will be searched. A value of 0 means no elements
         * @param XMLQueryMatcher matcher that indicates whether a TagArea with the correct tag type should be returned
         * @return a list of all child TagAreas that have a tag type equal to the provided tag type,
         * is not to deep and matches the given XMLQueryMatcher.
         */
        public List<TagArea> queryAllTag(String tagType, int maxDepth, XMLQueryMatcher matcher)
        {
            List<TagArea> tagAreas = new ArrayList<TagArea>();
            queryAllTagImpl(tagType, 0, maxDepth, tagAreas, matcher);
            return tagAreas;
        }

        public void queryAllTagImpl(String tagType, int depth, int maxDepth, List<TagArea> tagAreas, XMLQueryMatcher matcher)
        {
            depth++;
            if (depth > maxDepth)
                return;

            for (TagArea child : children)
            {
                if (child.getTagType().equalsIgnoreCase(tagType))
                {
                    if (matcher == null || matcher.matchesQuery(child))
                        tagAreas.add(child);
                }

                child.queryAllTagImpl(tagType, depth, maxDepth, tagAreas, matcher);
            }
        }

		public String getOriginalText() {
			// If this has no children and no value then this should be the whole tag
			if (this.children.size() == 0 && this.value == null)
			{
				if (!originalLine.endsWith("/>"))
				{
					return originalLine.substring(0,  originalLine.length() - 1) + "/>";
				}
				return originalLine;
			}

			StringBuffer sb = new StringBuffer();
			if (originalLine.endsWith("/>"))
			{
				sb.append(originalLine.substring(0, originalLine.length() - 2) +">\n");
			}
			else
				sb.append(originalLine +"\n");

			for (TagArea ta : this.children)
			{
				sb.append(ta.getOriginalText() + "\n");
			}

			if (this.value != null)
				sb.append(this.value + "\n");

			sb.append("</" + this.tagType +">");

			return sb.toString();
		}

		public TagArea(TagArea ta)
		{
	        this.value = ta.value;
	        this.tagType = ta.tagType;
	        this.originalLine = ta.originalLine;
	        this.children = new ArrayList<>();
	        this.params = new Hashtable<>();
	        this.children.addAll(ta.children);
	        this.params.putAll(ta.params);
		}
    }

	public static ArrayList<TagArea> process(String file) throws IOException
	{
		return XMLParser.process(ResourceManager.readAllLines(file));
	}

	public static ArrayList<TagArea> process(List<String> allLines) throws IOException
    {
        ArrayList<TagArea> parents = new ArrayList<TagArea>();

        Stack<TagArea> openTags = new Stack<TagArea>();
        for (String s : allLines)
        {
            s = s.trim();
            if (s.length() == 0 || s.startsWith("<!") || s.startsWith("<?"))
                continue;

            if (s.startsWith("</"))
            {
                openTags.pop();
            }
            else if (s.startsWith("<"))
            {
                TagArea ta = new TagArea(s);

                if (openTags.size() > 0)
                {
                    openTags.peek().children.add(ta);
                    openTags.push(ta);
                }
                else
                {
                    openTags.push(ta);
                    parents.add(ta);
                }

                if (s.endsWith("/>") || s.endsWith("</" + ta.tagType + ">"))
                {
                    openTags.pop();
                }
            }
            else
            {
                openTags.peek().value = s;
            }
        }
        return parents;
    }

    public interface XMLQueryMatcher
    {
        public boolean matchesQuery(TagArea tagArea);
    }


}

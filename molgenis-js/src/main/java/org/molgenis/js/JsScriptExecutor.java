package org.molgenis.js;

import org.mozilla.javascript.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;

/**
 * Executes a JavaScript
 */
@Service
public class JsScriptExecutor
{
	/**
	 * Execute a JavaScript
	 */
	public Object executeScript(String jsScript)
	{
		String functionName = "executeScript";
		String wrappedJsScript = String.format("function %s(){%s}", functionName, jsScript);
		Object result = ContextFactory.getGlobal().call(new ContextAction()
		{
			@Override
			public Object run(Context cx)
			{
				ScriptableObject scriptableObject = cx.initStandardObjects();
				try
				{
					cx.evaluateReader(scriptableObject, new StringReader(wrappedJsScript), null, 1, null);
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
				Function evalScript = (Function) scriptableObject.get("executeScript", scriptableObject);
				return evalScript.call(cx, scriptableObject, scriptableObject, new Object[] {});
			}

		});
		return result;
	}
}

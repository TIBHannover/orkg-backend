'use strict'

const { posix: path } = require('node:path')

const Asciidoctor = require('asciidoctor')
const asciidoctor = Asciidoctor();

// Spring REST Docs block macro to import multiple snippet of an operation at
// once
//
// Usage
//
//   operation::operation-name[snippets='snippet-name1,snippet-name2']
//
class OperationBlockMacro {
	constructor(context) {
		this.context = context
	}

	process(parent, operation, attributes) {
		const snippetsDir = String(parent.getDocument().getAttributes().snippets || '')
		const snippetNames = (attributes && Object.prototype.hasOwnProperty.call(attributes, 'snippets')) ? attributes.snippets : ''
		operation = parent.applySubstitutions(operation);
		const snippetTitles = new SnippetTitles(parent.getDocument().getAttributes())
		const content = this.readSnippets(snippetsDir, snippetNames, parent, operation, snippetTitles)
		if (content && content.length > 0) this.addBlocks(content, parent.getDocument(), parent)
		return null
	}

	readSnippets(snippetsDir, snippetNames, parent, operation, snippetTitles) {
		const snippets = this.snippetsToInclude(snippetNames, snippetsDir, operation)
		if (snippets.length === 0) {
			parent.getDocument().getLogger().warn(`No snippets were found for operation ${operation} in ${snippetsDir}`)
			return `No snippets found for operation::${operation}`
		}
		return this.doReadSnippets(snippets, parent, operation, snippetTitles)
	}

	doReadSnippets(snippets, parent, operation, snippetTitles) {
		const content = []
		const sectionId = parent.getId()
		snippets.forEach((snippet) => {
			this.appendSnippetBlock(content, snippet, sectionId, operation, snippetTitles, parent)
		})
		return content.join('')
	}

	addBlocks(content, doc, parent) {
		const options = { safe: doc.getOptions().safe, attributes: { ...doc.getAttributes() } }
		delete options.attributes.leveloffset
		const fragment = asciidoctor.load(content, options)
		// use a template to get the correct section name and level for blocks to append
		const template = asciidoctor.Section.create(parent, parent.level + 1, false, {})
		fragment.getBlocks().forEach((b) => {
			b.parent = parent
			// might be a standard block and no section in case of 'No snippets were found for operation'
			if (typeof b.setSectionName === 'function' && typeof b.setSectionName === 'function') {
				b.setSectionName(template.getSectionName())
			}
			if (typeof b.setLevel === 'function') b.setLevel(template.getLevel())
			parent.append(b)
		})

		if (typeof parent.findBy === 'function') {
			parent.findBy().forEach((b) => {
				if (b.getContext && b.getContext() !== 'document') {
					b.parent = b.getParent()
				}
			})
		}
	}

	snippetsToInclude(snippetNames, snippetsDir, operation) {
		if (!snippetNames || snippetNames.length === 0) {
			return this.allSnippets(snippetsDir, operation)
		}

		return snippetNames.split(',').map((name) => {
			const p = snippetsDir + path.join(operation, `${name}.adoc`)
			return new Snippet(p, name)
		})
	}

	allSnippets(snippetsDir, operation) {
		throw Error("Including all snippets is currently not supported. Please specify individual snippet names you want to include using the 'snippets' attribute.")
	}

	appendSnippetBlock(content, snippet, sectionId, operation, snippetTitles, parent) {
		this.writeTitle(content, snippet, sectionId, snippetTitles)
		this.writeContent(content, snippet, operation, parent)
	}

	writeContent(content, snippet, operation, parent) {
		const { file, contentCatalog } = this.context;
		const INCLUDE_FAMILIES = ['attachment', 'example', 'partial']
		const resource = contentCatalog.resolveResource(snippet.path, file.src, undefined, INCLUDE_FAMILIES)

		if (resource) {
			content.push(resource.contents.toString())
			if (!content[content.length - 1].endsWith('\n')) content.push('\n')
		} else {
			parent.getDocument().getLogger().warn(`Snippet ${snippet.name} not found at ${snippet.path} for operation ${operation}`)
			content.push(`Snippet ${snippet.name} not found for operation::${operation}\n`)
			content.push('\n')
		}
	}

	writeTitle(content, snippet, id, snippetTitles) {
		const sectionLevel = '=='
		const title = snippetTitles.titleForSnippet(snippet)
		content.push(`[[${id}_${snippet.name.replace('-', '_')}]]\n`)
		content.push(`${sectionLevel} ${title}\n`)
		content.push('\n')
	}
}

class Snippet {
	constructor(p, name) {
		this.path = p
		this.name = name
	}
}

class SnippetTitles {
	static defaults = {
		'http-request': 'HTTP request',
		'curl-request': 'Curl request',
		'httpie-request': 'HTTPie request',
		'request-body': 'Request body',
		'request-fields': 'Request fields',
		'http-response': 'HTTP response',
		'response-body': 'Response body',
		'response-fields': 'Response fields',
		'links': 'Links'
	}

	constructor(documentAttributes) {
		this.documentAttributes = documentAttributes
	}

	titleForSnippet(snippet) {
		const attributeName = `operation-${snippet.name}-title`
		if (Object.prototype.hasOwnProperty.call(this.documentAttributes, attributeName)) {
			return this.documentAttributes[attributeName]
		}
		if (Object.prototype.hasOwnProperty.call(SnippetTitles.defaults, snippet.name)) {
			return SnippetTitles.defaults[snippet.name]
		}
		return capitalize(snippet.name.replace('-', ' '))
	}
}

function capitalize(s) {
	if (!s || s.length === 0) return s
	return s.charAt(0).toUpperCase() + s.slice(1)
}

function register(registry, context) {
	registry.blockMacro(function() {
		const self = this
		self.named('operation');
		const processor = new OperationBlockMacro(context);

		self.process((parent, target, attrs) => processor.process(parent, target, attrs))
	});
	return registry;
}

module.exports = { register }

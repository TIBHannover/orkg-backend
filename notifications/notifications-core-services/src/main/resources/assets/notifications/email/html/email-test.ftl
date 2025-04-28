<#import "template.ftl" as layout>
<@layout.emailLayout>
${formatMessage("email-test-body-html", message)?no_esc}
</@layout.emailLayout>

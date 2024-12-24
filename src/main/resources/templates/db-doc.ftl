<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<?mso-application progid="Word.Document"?>
<w:wordDocument xmlns:w="http://schemas.microsoft.com/office/word/2003/wordml">
    <w:body>
        <w:p>
            <w:r>
                <w:t>数据库设计文档</w:t>
            </w:r>
        </w:p>
        <w:p>
            <w:r>
                <w:t>生成时间：${generateTime}</w:t>
            </w:r>
        </w:p>
        
        <#list tables as table>
        <w:p>
            <w:r>
                <w:t>表名：${table.tableName}</w:t>
            </w:r>
        </w:p>
        <w:p>
            <w:r>
                <w:t>说明：${table.tableComment!''}</w:t>
            </w:r>
        </w:p>
        <w:tbl>
            <w:tr>
                <w:tc><w:p><w:r><w:t>列名</w:t></w:r></w:p></w:tc>
                <w:tc><w:p><w:r><w:t>类型</w:t></w:r></w:p></w:tc>
                <w:tc><w:p><w:r><w:t>长度</w:t></w:r></w:p></w:tc>
                <w:tc><w:p><w:r><w:t>允许空</w:t></w:r></w:p></w:tc>
                <w:tc><w:p><w:r><w:t>主键</w:t></w:r></w:p></w:tc>
                <w:tc><w:p><w:r><w:t>说明</w:t></w:r></w:p></w:tc>
            </w:tr>
            <#list table.columns as column>
            <w:tr>
                <w:tc><w:p><w:r><w:t>${column.columnName}</w:t></w:r></w:p></w:tc>
                <w:tc><w:p><w:r><w:t>${column.dataType}</w:t></w:r></w:p></w:tc>
                <w:tc><w:p><w:r><w:t>${column.columnSize}</w:t></w:r></w:p></w:tc>
                <w:tc><w:p><w:r><w:t>${column.nullable?string('是','否')}</w:t></w:r></w:p></w:tc>
                <w:tc><w:p><w:r><w:t>${column.primaryKey?string('是','否')}</w:t></w:r></w:p></w:tc>
                <w:tc><w:p><w:r><w:t>${column.columnComment!''}</w:t></w:r></w:p></w:tc>
            </w:tr>
            </#list>
        </w:tbl>
        </#list>
    </w:body>
</w:wordDocument> 
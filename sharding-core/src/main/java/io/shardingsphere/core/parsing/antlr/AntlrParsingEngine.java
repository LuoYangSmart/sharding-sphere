/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.ast.SQLASTParserFactory;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLStatementExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLStatementExtractorFactory;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLStatementType;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;
import io.shardingsphere.core.parsing.parser.sql.SQLParser;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AllArgsConstructor;

/**
 * Parsing engine for Antlr.
 *
 * @author duhongjun
 */
@AllArgsConstructor
public final class AntlrParsingEngine implements SQLParser {
    
    private DatabaseType dbType;
    
    private String sql;
    
    private ShardingRule shardingRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public SQLStatement parse() {
        ParserRuleContext rootContext = SQLASTParserFactory.newInstance(dbType, sql).execute();
        if (null == rootContext) {
            throw new SQLParsingUnsupportedException(String.format("Unsupported SQL of `%s`", sql));
        }
        if (rootContext.getChild(0) instanceof ErrorNodeImpl) {
            throw new SQLParsingUnsupportedException(String.format("Unsupported SQL of `%s`", sql));
        }
        ParserRuleContext parserRuleContext = (ParserRuleContext) rootContext.getChild(0);
        SQLStatementExtractor extractor = SQLStatementExtractorFactory.getInstance(dbType, SQLStatementType.nameOf(parserRuleContext.getClass().getSimpleName()));
        if (null == extractor) {
            throw new SQLParsingUnsupportedException(String.format("Unsupported SQL statement of `%s`", parserRuleContext.getClass().getSimpleName()));
        }
        return extractor.extract(sql, parserRuleContext, shardingRule, shardingTableMetaData);
    }
}

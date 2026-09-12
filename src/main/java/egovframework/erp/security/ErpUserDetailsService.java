package egovframework.erp.security;

import egovframework.erp.security.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ErpUserDetailsService implements UserDetailsService {

    private final AccountMapper accountMapper;

    @Autowired
    public ErpUserDetailsService(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountVO account = accountMapper.selectByUsername(username);
        if (account == null) {
            throw new UsernameNotFoundException("존재하지 않는 계정입니다: " + username);
        }
        return new ErpUserDetails(account);
    }
}
